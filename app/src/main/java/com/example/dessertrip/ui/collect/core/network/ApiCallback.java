package com.example.dessertrip.ui.collect.core.network;

/** 비동기 API 결과 콜백 (메인 스레드에서 호출됨). 204 응답이면 onSuccess(null). */
public interface ApiCallback<T> {
    void onSuccess(T data);
    void onError(ApiError error);
}
