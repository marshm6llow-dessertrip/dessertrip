package com.example.dessertrip.ui.collect.core.network;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private final TokenStore tokenStore;

    public AuthInterceptor(TokenStore tokenStore) { this.tokenStore = tokenStore; }

    @Override public Response intercept(Chain chain) throws IOException {
        String token = tokenStore.getAccessToken();
        Request request = chain.request();
        if (token != null && !token.isEmpty()) {
            request = request.newBuilder().header("Authorization", "Bearer " + token).build();
        }
        return chain.proceed(request);
    }
}
