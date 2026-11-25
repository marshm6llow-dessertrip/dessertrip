package com.example.dessertrip.ui.collect.core.network;

import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Retrofit 응답 → ApiCallback 변환 + 상태 코드별 오류 처리.
 *
 * 200/201/204        : 성공 (204 는 onSuccess(null))
 * 400                : 서버 message / errors(field) 를 그대로 보여줌 (원인 파악용)
 * 401                : 인증 필요 — UnauthorizedInterceptor 가 세션 만료 이벤트도 발행
 * 403, 404           : 서버 message 표시
 * 5xx                : 서버 message 는 노출하지 않고 공통 문구만 표시 (내부 오류 은닉)
 */
public class ApiErrorParser {
    private static final String TAG = "Api";
    private final Gson gson;

    public ApiErrorParser(Gson gson) { this.gson = gson; }

    /** 호출을 비동기로 실행하고 Call 을 돌려준다(취소용). 취소된 호출은 콜백을 부르지 않는다. */
    public <T> Call<T> enqueue(Call<T> call, final ApiCallback<T> cb) {
        call.enqueue(new Callback<T>() {
            @Override public void onResponse(Call<T> c, Response<T> res) {
                if (res.isSuccessful()) {
                    T body = res.body();
                    if (body != null || res.code() == 204 || res.code() == 205) cb.onSuccess(body);
                    else cb.onError(unknown());
                } else {
                    String raw = null;
                    try { raw = res.errorBody() == null ? null : res.errorBody().string(); } catch (IOException ignored) { }
                    cb.onError(fromHttp(res.code(), raw));
                }
            }

            @Override public void onFailure(Call<T> c, Throwable t) {
                if (c.isCanceled()) return;
                cb.onError(fromThrowable(t));
            }
        });
        return call;
    }

    public ApiError fromHttp(int status, String raw) {
        ErrorResponse body = null;
        if (raw != null && !raw.trim().isEmpty()) {
            try { body = gson.fromJson(raw, ErrorResponse.class); } catch (Exception ignored) { }
        }
        // 로그에는 상태/코드만 남긴다. (토큰, 본문 등 민감 정보 로깅 금지)
        Log.w(TAG, "HTTP " + status + " code=" + (body == null ? null : body.code));

        String serverMessage = body != null && body.message != null && !body.message.trim().isEmpty() ? body.message : null;
        ApiErrorType type;
        String message;
        if (status == 400) { type = ApiErrorType.BAD_REQUEST; message = serverMessage != null ? serverMessage : "요청 내용을 다시 확인해 주세요."; }
        else if (status == 401) { type = ApiErrorType.UNAUTHORIZED; message = "로그인이 필요하거나 인증이 만료됐어요."; }
        else if (status == 403) { type = ApiErrorType.FORBIDDEN; message = serverMessage != null ? serverMessage : "이 작업을 할 권한이 없어요."; }
        else if (status == 404) { type = ApiErrorType.NOT_FOUND; message = serverMessage != null ? serverMessage : "요청한 데이터를 찾을 수 없어요."; }
        else if (status >= 500 && status < 600) { type = ApiErrorType.SERVER; message = "일시적인 문제가 발생했어요. 잠시 후 다시 시도해 주세요."; }
        else { type = ApiErrorType.UNKNOWN; message = "알 수 없는 오류가 발생했어요."; }

        Map<String, String> fields = new HashMap<>();
        if (body != null && body.errors != null) {
            for (ErrorResponse.FieldError e : body.errors) {
                if (e != null && e.field != null) fields.put(e.field, e.reason);
            }
        }
        return new ApiError(type, message, status, body == null ? null : body.code, fields);
    }

    public ApiError fromThrowable(Throwable t) {
        if (t instanceof SocketTimeoutException) {
            return new ApiError(ApiErrorType.TIMEOUT, "서버 응답이 늦어지고 있어요. 잠시 후 다시 시도해 주세요.");
        }
        if (t instanceof IOException) {
            return new ApiError(ApiErrorType.NETWORK, "네트워크 연결을 확인해 주세요.");
        }
        return unknown();
    }

    /** 서버로 보내기 전에 클라이언트에서 막은 경우(이미지 형식/용량 등). */
    public ApiError clientError(String message, String field) {
        Map<String, String> fields = new HashMap<>();
        if (field != null) fields.put(field, message);
        return new ApiError(ApiErrorType.BAD_REQUEST, message, null, null, fields);
    }

    private ApiError unknown() { return new ApiError(ApiErrorType.UNKNOWN, "알 수 없는 오류가 발생했어요."); }
}
