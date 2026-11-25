package com.dessertrip.dessertrip_backend.common.collect;

import java.util.Set;

/** 이미지 업로드 규칙. TODO: 허용 형식과 파일별 최대 용량은 팀 협의 후 확정 (현재는 임시값) */
public final class UploadPolicy {
    private UploadPolicy() { }

    public static final long MAX_IMAGE_SIZE_BYTES = 10L * 1024 * 1024;   // 파일별 최대 10MB
    public static final int MAX_IMAGE_COUNT = 10;                        // 테이스팅 노트 사진 최대 장수
    public static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
}
