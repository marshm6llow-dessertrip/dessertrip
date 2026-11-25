package com.example.dessertrip.ui.collect.core.network;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

public class UnauthorizedInterceptor implements Interceptor {
    private final SessionEvents events;

    public UnauthorizedInterceptor(SessionEvents events) { this.events = events; }

    @Override public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        if (response.code() == 401) events.notifyUnauthorized();
        return response;
    }
}
