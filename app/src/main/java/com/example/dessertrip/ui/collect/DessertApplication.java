package com.example.dessertrip.ui.collect;

import android.app.Application;
import android.content.Context;

public class DessertApplication extends Application {
    private AppContainer container;

    @Override public void onCreate() {
        super.onCreate();
        container = new AppContainer(this);
    }

    public static AppContainer container(Context context) {
        return ((DessertApplication) context.getApplicationContext()).container;
    }
}
