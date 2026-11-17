package com.dessertrip.dessertrip_backend.common;

import java.util.List;

/** 팀 공통 오류 응답 형식: { status, code, message, errors:[{field, reason}] } */
public record ErrorResponse(int status, String code, String message, List<FieldError> errors) {

    /** 어떤 입력 필드가 왜 잘못됐는지 */
    public record FieldError(String field, String reason) { }

    public static ErrorResponse of(ErrorCode errorCode, String message, List<FieldError> errors) {
        return new ErrorResponse(errorCode.getStatus().value(), errorCode.name(), message, errors);
    }
}