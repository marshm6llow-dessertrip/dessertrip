package com.example.dessertrip.ui.collect.core.network;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.dessertrip.ui.collect.core.util.Event;

/** 401 발생 시 앱 전역에 알린다. (로그인 화면이 생기면 MainActivity 에서 이동 처리) */
public class SessionEvents {
    private final MutableLiveData<Event<Boolean>> unauthorized = new MutableLiveData<>();

    public LiveData<Event<Boolean>> getUnauthorized() { return unauthorized; }

    /** OkHttp 스레드에서 호출되므로 postValue 사용 */
    public void notifyUnauthorized() { unauthorized.postValue(new Event<>(true)); }
}
