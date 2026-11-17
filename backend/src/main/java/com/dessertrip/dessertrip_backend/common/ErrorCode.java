package com.dessertrip.dessertrip_backend.common;

import org.springframework.http.HttpStatus;

/** 오류 코드 목록. 작성 방식: 대문자 스네이크(UPPER_SNAKE_CASE). 기능을 추가할 때 여기와 API 명세서를 함께 수정한다. */
public enum ErrorCode {
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다."),
    INVALID_JSON(HttpStatus.BAD_REQUEST, "요청 본문이 올바른 JSON 형식이 아닙니다."),
    INVALID_CONTENT_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 Content-Type 입니다."),
    EMAIL_DUPLICATED(HttpStatus.BAD_REQUEST, "이미 가입된 이메일입니다."),

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "인증 정보가 올바르지 않거나 만료되었습니다. 다시 로그인해 주세요."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    NOT_FOUND(HttpStatus.NOT_FOUND, "대상을 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "허용되지 않는 요청 방식입니다."),

    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String defaultMessage) {
        this.status = status;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus getStatus() { return status; }

    public String getDefaultMessage() { return defaultMessage; }
}