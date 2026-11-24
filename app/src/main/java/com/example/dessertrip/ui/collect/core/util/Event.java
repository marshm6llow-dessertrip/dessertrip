package com.example.dessertrip.ui.collect.core.util;

/** LiveData 로 일회성 이벤트(스낵바 메시지 등)를 전달하기 위한 래퍼 */
public class Event<T> {
    private final T content;
    private boolean handled;

    public Event(T content) { this.content = content; }

    /** 아직 처리되지 않았다면 내용을 돌려주고, 이후에는 null */
    public T getContentIfNotHandled() {
        if (handled) return null;
        handled = true;
        return content;
    }
}
