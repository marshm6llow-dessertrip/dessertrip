package com.example.dessertrip.ui.collect.core.network;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** 이미지 업로드 정책 — 팀 협의 후 확정 (현재 값은 임시) */
public final class UploadPolicy {
    private UploadPolicy() { }

    public static final Set<String> ALLOWED_MIME =
            new HashSet<>(Arrays.asList("image/jpeg", "image/png", "image/webp")); // TODO 팀 협의 후 확정
    public static final long MAX_BYTES = 10L * 1024 * 1024;                          // TODO 파일별 최대 용량 확정
    public static final int MAX_PHOTOS = 10;
}
