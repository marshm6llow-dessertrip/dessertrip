package com.example.dessertrip.ui.archive.sticker;

import com.example.dessertrip.ui.collect.core.image.BitmapLoader;
import com.example.dessertrip.ui.collect.core.image.StickerMath;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.google.android.gms.common.moduleinstall.ModuleAvailabilityResponse;
import com.google.android.gms.common.moduleinstall.ModuleInstall;
import com.google.android.gms.common.moduleinstall.ModuleInstallClient;
import com.google.android.gms.common.moduleinstall.ModuleInstallRequest;
import com.google.android.gms.tasks.Tasks;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation;
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentationResult;
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenter;
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 사진에서 디저트만 남기고 배경을 지워 투명 PNG 스티커로 만든다. (ML Kit 주제 분할 / Subject Segmentation)
 *
 * - 처리는 모두 기기 안에서 한다. 사진이 서버로 나가지 않고, 서버에는 완성된 스티커 PNG 만 올린다.
 * - 모델은 Google Play 서비스가 내려받는다(앱 용량 약 200KB 증가). 첫 사용 때 다운로드가 필요할 수 있다.
 * - 베타 API 라서 SLA 가 없고 변경될 수 있다. 문제가 생기면 이 클래스만 교체하면 되도록 호출부를 좁혀 두었다.
 * - 같은 이유로 ML Kit 타입을 이 클래스 밖으로 내보내지 않는다.
 */
public class BackgroundRemover {

    public interface Callback {
        /** 투명 배경 PNG 파일(앱 캐시). 사용이 끝나면 호출한 쪽에서 지운다. */
        void onSuccess(File png);
        /** 사용자에게 그대로 보여 줄 수 있는 메시지 */
        void onError(String userMessage);
    }

    /** ML Kit 은 512x512 이상에서 정확하다. 너무 크면 느리고 메모리를 많이 쓰므로 긴 변을 1280 으로 맞춘다. */
    static final int MAX_INPUT_EDGE = 1280;
    /** 스티커 둘레 여백(px) */
    private static final int STICKER_PADDING = 12;
    /** 이 비율보다 적게 남으면 디저트를 못 찾은 것으로 본다 */
    private static final float MIN_SUBJECT_RATIO = 0.01f;
    private static final int MIN_BOX_ALPHA = 16;
    private static final long MODEL_CHECK_SECONDS = 15;
    private static final long MODEL_INSTALL_SECONDS = 120;
    private static final long SEGMENT_SECONDS = 60;
    private static final long STALE_FILE_MS = 24L * 60 * 60 * 1000;

    private final Context appContext;
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private SubjectSegmenter segmenter;   // 지연 생성, 앱이 살아 있는 동안 재사용(첫 추론이 느리므로)

    public BackgroundRemover(Context context) {
        this.appContext = context.getApplicationContext();
    }

    /** 화면에 들어올 때 미리 불러 둔다: 분할기 초기화 + (필요하면) 모델 다운로드. 실패해도 조용히 넘어가고 실제 처리 때 다시 시도한다. */
    public void warmUp() {
        io.execute(() -> {
            deleteStaleFiles();
            try {
                ensureModel(getSegmenter());
            } catch (Exception ignored) {
                // 오프라인/Play 서비스 없음 등: 실제 처리에서 사용자에게 안내한다
            }
        });
    }

    /** 비동기로 처리한다. 반환된 Future 를 cancel 하면 진행 중인 작업을 중단하고 콜백도 부르지 않는다. */
    public Future<?> remove(final Uri photo, final Callback cb) {
        return io.submit(() -> {
            try {
                File png = process(photo);
                post(() -> cb.onSuccess(png), png);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();      // 취소됨: 콜백 없음
            } catch (final RemovalException e) {
                post(() -> cb.onError(e.getMessage()), null);
            } catch (final Throwable t) {
                post(() -> cb.onError(messageFor(t)), null);
            }
        });
    }

    // ------------------------------------------------------------------ 처리

    private File process(Uri photo) throws Exception {
        Bitmap src = BitmapLoader.load(appContext.getContentResolver(), photo, MAX_INPUT_EDGE);
        try {
            SubjectSegmenter seg = getSegmenter();
            ensureModel(seg);

            SubjectSegmentationResult result = Tasks.await(
                    seg.process(InputImage.fromBitmap(src, 0)), SEGMENT_SECONDS, TimeUnit.SECONDS);
            FloatBuffer mask = result.getForegroundConfidenceMask();
            if (mask == null) throw new RemovalException("디저트를 찾지 못했어요. 다른 사진으로 다시 시도해 주세요.");

            int w = src.getWidth(), h = src.getHeight();
            int[] pixels = new int[w * h];
            src.getPixels(pixels, 0, w, 0, 0, w, h);
            int visible = StickerMath.applyMask(pixels, mask);
            if (visible < w * h * MIN_SUBJECT_RATIO) {
                throw new RemovalException("디저트를 찾지 못했어요. 디저트가 또렷하게 나온 사진으로 다시 시도해 주세요.");
            }

            int[] box = StickerMath.findBounds(pixels, w, h, MIN_BOX_ALPHA);
            if (box == null) throw new RemovalException("디저트를 찾지 못했어요. 다른 사진으로 다시 시도해 주세요.");
            box = StickerMath.pad(box, w, h, STICKER_PADDING);
            int bw = box[2] - box[0], bh = box[3] - box[1];

            Bitmap sticker = Bitmap.createBitmap(bw, bh, Bitmap.Config.ARGB_8888);
            try {
                sticker.setPixels(pixels, box[1] * w + box[0], w, 0, 0, bw, bh);
                return writePng(sticker);
            } finally {
                sticker.recycle();
            }
        } finally {
            src.recycle();
        }
    }

    private File writePng(Bitmap bitmap) throws IOException {
        File dir = new File(appContext.getCacheDir(), "stickers");
        if (!dir.exists() && !dir.mkdirs()) throw new IOException("cache dir");
        File file = File.createTempFile("sticker_", ".png", dir);
        try (FileOutputStream out = new FileOutputStream(file)) {
            if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) throw new IOException("png encode failed");
        } catch (IOException e) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
            throw e;
        }
        return file;
    }

    // ------------------------------------------------------------------ ML Kit

    private synchronized SubjectSegmenter getSegmenter() {
        if (segmenter == null) {
            // 필요한 결과만 켜서 메모리를 아낀다. 투명 비트맵은 마스크로 직접 만든다(가장자리 처리·잘라내기를 제어하려고).
            SubjectSegmenterOptions options = new SubjectSegmenterOptions.Builder()
                    .enableForegroundConfidenceMask()
                    .build();
            segmenter = SubjectSegmentation.getClient(options);
        }
        return segmenter;
    }

    /** 모델이 기기에 없으면 Google Play 서비스에 내려받기를 요청하고 끝날 때까지 기다린다. */
    private void ensureModel(SubjectSegmenter seg) throws Exception {
        ModuleInstallClient client = ModuleInstall.getClient(appContext);
        ModuleAvailabilityResponse availability =
                Tasks.await(client.areModulesAvailable(seg), MODEL_CHECK_SECONDS, TimeUnit.SECONDS);
        if (availability.areModulesAvailable()) return;
        ModuleInstallRequest request = ModuleInstallRequest.newBuilder().addApi(seg).build();
        Tasks.await(client.installModules(request), MODEL_INSTALL_SECONDS, TimeUnit.SECONDS);
    }

    // ------------------------------------------------------------------ 보조

    private void post(Runnable r, final File fileToDeleteIfDropped) {
        if (!main.post(r) && fileToDeleteIfDropped != null) {
            //noinspection ResultOfMethodCallIgnored
            fileToDeleteIfDropped.delete();
        }
    }

    private void deleteStaleFiles() {
        File[] files = new File(appContext.getCacheDir(), "stickers").listFiles();
        if (files == null) return;
        long limit = System.currentTimeMillis() - STALE_FILE_MS;
        for (File f : files) {
            //noinspection ResultOfMethodCallIgnored
            if (f.lastModified() < limit) f.delete();
        }
    }

    private static String messageFor(Throwable t) {
        Throwable cause = t instanceof ExecutionException && t.getCause() != null ? t.getCause() : t;
        if (cause instanceof OutOfMemoryError) return "사진이 너무 커서 처리하지 못했어요. 더 작은 사진으로 시도해 주세요.";
        if (cause instanceof TimeoutException) return "배경 제거가 너무 오래 걸려요. 잠시 후 다시 시도해 주세요.";
        if (cause instanceof IOException) return "이미지를 읽을 수 없어요.";
        // 모델 다운로드 실패, Google Play 서비스 없음/오래됨 등
        return "배경 제거를 준비하지 못했어요. 인터넷에 연결되어 있는지, Google Play 서비스가 있는 기기인지 확인해 주세요.";
    }

    /** 사용자에게 그대로 보여 줄 수 있는 실패 */
    private static final class RemovalException extends Exception {
        private static final long serialVersionUID = 1L;
        RemovalException(String userMessage) { super(userMessage); }
    }
}
