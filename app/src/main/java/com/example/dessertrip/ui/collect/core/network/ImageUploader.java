package com.example.dessertrip.ui.collect.core.network;

import android.content.ContentResolver;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * Uri → multipart/form-data 파트. 파일 I/O 가 있으므로 백그라운드 스레드에서 호출한다.
 * 형식·용량은 서버에서도 다시 검증한다(클라이언트는 사전 안내용).
 */
public class ImageUploader {
    private final ContentResolver resolver;
    private final ApiErrorParser parser;

    public ImageUploader(ContentResolver resolver, ApiErrorParser parser) {
        this.resolver = resolver;
        this.parser = parser;
    }

    public MultipartBody.Part toPart(String fieldName, Uri uri) throws UploadException {
        String mime = resolveMime(uri);
        if (!UploadPolicy.ALLOWED_MIME.contains(mime)) {
            throw new UploadException(parser.clientError("JPG, PNG, WEBP 형식의 이미지만 올릴 수 있어요.", fieldName));
        }
        byte[] bytes;
        try (InputStream in = resolver.openInputStream(uri)) {
            if (in == null) throw new IOException("null stream");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) != -1) {
                out.write(buf, 0, n);
                if (out.size() > UploadPolicy.MAX_BYTES) break;
            }
            bytes = out.toByteArray();
        } catch (IOException | SecurityException e) {
            throw new UploadException(parser.clientError("이미지를 읽을 수 없어요.", fieldName));
        }
        if (bytes.length > UploadPolicy.MAX_BYTES) {
            long mb = UploadPolicy.MAX_BYTES / 1024 / 1024;
            throw new UploadException(parser.clientError("이미지 용량은 " + mb + "MB 이하여야 해요.", fieldName));
        }
        String ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime);
        RequestBody body = RequestBody.create(bytes, MediaType.parse(mime));
        return MultipartBody.Part.createFormData(fieldName, "image." + (ext == null ? "jpg" : ext), body);
    }

    private String resolveMime(Uri uri) {
        String type = resolver.getType(uri);
        if (type != null) return type;
        String ext = MimeTypeMap.getFileExtensionFromUrl(uri.toString()).toLowerCase(Locale.ROOT);
        String guess = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
        return guess == null ? "application/octet-stream" : guess;
    }
}
