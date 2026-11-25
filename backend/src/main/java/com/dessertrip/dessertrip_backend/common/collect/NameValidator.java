package com.dessertrip.dessertrip_backend.common.collect;

import com.dessertrip.dessertrip_backend.common.ApiException;
import com.dessertrip.dessertrip_backend.common.ErrorCode;

/** 카테고리/태그 이름 규칙: 앞뒤 공백 제거 후 1~20자 */
public final class NameValidator {

    private NameValidator() { }

    public static String clean(String raw, String field) {
        String name = raw == null ? "" : raw.trim();
        if (name.isEmpty()) throw ApiException.ofField(ErrorCode.VALIDATION_FAILED, field, "필수 입력값입니다.");
        if (name.length() > NamePolicy.MAX_NAME_LENGTH) {
            throw ApiException.ofField(ErrorCode.VALIDATION_FAILED, field, NamePolicy.MAX_NAME_LENGTH + "자 이하여야 합니다.");
        }
        return name;
    }
}
