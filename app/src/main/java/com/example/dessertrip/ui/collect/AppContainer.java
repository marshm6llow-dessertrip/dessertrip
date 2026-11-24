package com.example.dessertrip.ui.collect;

import com.example.dessertrip.BuildConfig;
import android.content.Context;

import com.example.dessertrip.ui.archive.sticker.BackgroundRemover;
import com.example.dessertrip.ui.collect.core.network.ApiErrorParser;
import com.example.dessertrip.ui.collect.core.network.AuthInterceptor;
import com.example.dessertrip.ui.collect.core.network.ImageUploader;
import com.example.dessertrip.ui.collect.core.network.SessionEvents;
import com.example.dessertrip.ui.collect.core.network.TokenStore;
import com.example.dessertrip.ui.collect.core.network.UnauthorizedInterceptor;
import com.example.dessertrip.ui.collect.data.DessertApi;
import com.example.dessertrip.ui.collect.data.DessertRepository;

import com.google.gson.Gson;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/** 간단한 수동 DI. (Hilt 도입 시 이 클래스를 Module 로 옮기면 된다) */
public class AppContainer {
    public final TokenStore tokenStore;
    public final SessionEvents sessionEvents = new SessionEvents();
    public final DessertRepository repository;
    public final BackgroundRemover backgroundRemover;

    public AppContainer(Context context) {
        tokenStore = new TokenStore(context);
        Gson gson = new Gson();
        ApiErrorParser parser = new ApiErrorParser(gson);

        OkHttpClient.Builder http = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(tokenStore))
                .addInterceptor(new UnauthorizedInterceptor(sessionEvents))
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS); // 이미지 업로드
        if (BuildConfig.DEBUG) {
            // BASIC: 본문을 찍지 않고, Authorization 헤더는 가려서 민감 정보 노출 방지
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
            logging.redactHeader("Authorization");
            http.addInterceptor(logging);
        }

        DessertApi api = new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(http.build())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(DessertApi.class);

        repository = new DessertRepository(api, parser,
                new ImageUploader(context.getContentResolver(), parser), gson);
        backgroundRemover = new BackgroundRemover(context);
    }
}
