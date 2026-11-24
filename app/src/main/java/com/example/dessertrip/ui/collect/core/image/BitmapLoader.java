package com.example.dessertrip.ui.collect.core.image;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;

/** Uri → 회전(EXIF)이 바로잡힌, 긴 변이 maxEdge 이하인 비트맵. 파일 I/O 가 있으므로 백그라운드 스레드에서 호출한다. */
public final class BitmapLoader {
    private BitmapLoader() { }

    public static Bitmap load(ContentResolver resolver, Uri uri, int maxEdge) throws IOException {
        // 1) 크기만 읽는다
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream in = open(resolver, uri)) {
            BitmapFactory.decodeStream(in, null, bounds);
        }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) throw new IOException("not an image");

        // 2) 메모리를 아끼도록 줄여서 디코딩
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = StickerMath.sampleSize(bounds.outWidth, bounds.outHeight, maxEdge);
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap;
        try (InputStream in = open(resolver, uri)) {
            bitmap = BitmapFactory.decodeStream(in, null, opts);
        }
        if (bitmap == null) throw new IOException("decode failed");

        // 3) 정확히 maxEdge 에 맞추고, 촬영 방향(EXIF)대로 돌린다
        int[] size = StickerMath.fitWithin(bitmap.getWidth(), bitmap.getHeight(), maxEdge);
        if (size[0] != bitmap.getWidth() || size[1] != bitmap.getHeight()) {
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, size[0], size[1], true);
            if (scaled != bitmap) bitmap.recycle();
            bitmap = scaled;
        }
        return rotateByExif(resolver, uri, bitmap);
    }

    private static Bitmap rotateByExif(ContentResolver resolver, Uri uri, Bitmap bitmap) {
        int orientation = ExifInterface.ORIENTATION_NORMAL;
        try (InputStream in = open(resolver, uri)) {
            orientation = new ExifInterface(in).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        } catch (IOException | RuntimeException ignored) {
            // EXIF 를 못 읽으면 회전 없이 진행
        }
        Matrix m = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90: m.postRotate(90); break;
            case ExifInterface.ORIENTATION_ROTATE_180: m.postRotate(180); break;
            case ExifInterface.ORIENTATION_ROTATE_270: m.postRotate(270); break;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL: m.postScale(-1, 1); break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL: m.postScale(1, -1); break;
            case ExifInterface.ORIENTATION_TRANSPOSE: m.postRotate(90); m.postScale(-1, 1); break;
            case ExifInterface.ORIENTATION_TRANSVERSE: m.postRotate(270); m.postScale(-1, 1); break;
            default: return bitmap;
        }
        Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
        if (rotated != bitmap) bitmap.recycle();
        return rotated;
    }

    private static InputStream open(ContentResolver resolver, Uri uri) throws IOException {
        InputStream in = resolver.openInputStream(uri);
        if (in == null) throw new IOException("null stream");
        return in;
    }
}
