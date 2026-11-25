package com.example.dessertrip.ui.collect.core.network;

import java.util.Collections;
import java.util.Map;

/** 화면(ViewModel)에서 쓰는 오류 모델. userMessage 는 사용자에게 그대로 보여줘도 안전한 문구. */
public class ApiError {
    public final ApiErrorType type;
    public final String userMessage;
    public final Integer httpStatus;
    public final String code;
    public final Map<String, String> fieldErrors;

    public ApiError(ApiErrorType type, String userMessage, Integer httpStatus, String code, Map<String, String> fieldErrors) {
        this.type = type;
        this.userMessage = userMessage;
        this.httpStatus = httpStatus;
        this.code = code;
        this.fieldErrors = fieldErrors == null ? Collections.<String, String>emptyMap() : fieldErrors;
    }

    public ApiError(ApiErrorType type, String userMessage) {
        this(type, userMessage, null, null, null);
    }
}
