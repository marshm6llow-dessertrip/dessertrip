package com.example.dessertrip.ui.collect.core.network;

import java.util.ArrayList;
import java.util.List;

/**
 * 서버 공통 오류 응답 형식 (팀 합의용 제안안 — API_SPEC.md 참고)
 * { "status": 400, "code": "VALIDATION_FAILED", "message": "...",
 *   "errors": [ { "field": "dessertName", "reason": "필수 입력값입니다." } ] }
 * 서버 내부 오류/민감 정보는 응답에 넣지 않는 것이 규칙이며, 앱도 5xx message 는 화면에 노출하지 않는다.
 */
public class ErrorResponse {
    public Integer status;
    public String code;
    public String message;
    public List<FieldError> errors = new ArrayList<>();

    public static class FieldError {
        public String field;
        public String reason;
    }
}
