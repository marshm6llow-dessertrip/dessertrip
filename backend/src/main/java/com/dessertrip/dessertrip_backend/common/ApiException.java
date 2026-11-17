/*throw new ApiException(ErrorCode.XXX) 를 받아 처리 */
package com.dessertrip.dessertrip_backend.common;

import java.util.List;

public class ApiException extends RuntimeException {
    private final ErrorCode errorCode;
    private final List<ErrorResponse.FieldError> errors;

    public ApiException(ErrorCode errorCode) {
        this(errorCode, errorCode.getDefaultMessage(), List.of());
    }

    public ApiException(ErrorCode errorCode, String message, List<ErrorResponse.FieldError> errors) {
        super(message);
        this.errorCode = errorCode;
        this.errors = errors;
    }

    /** 특정 필드가 잘못됐을 때: 오류 코드의 기본 문구 + 필드별 사유 */
    public static ApiException ofField(ErrorCode errorCode, String field, String reason) {
        return new ApiException(errorCode, errorCode.getDefaultMessage(),
                List.of(new ErrorResponse.FieldError(field, reason)));
    }

    public ErrorCode getErrorCode() { return errorCode; }

    public List<ErrorResponse.FieldError> getErrors() { return errors; }
}