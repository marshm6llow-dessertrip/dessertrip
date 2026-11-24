package com.dessertrip.dessertrip_backend.common.collect;

/** 이름/글자 수 규칙. TODO: 팀 협의 후 확정 (현재는 임시값, 앱의 상수와 같아야 한다) */
public final class NamePolicy {
    private NamePolicy() { }

    public static final int MAX_DESSERT_NAME_LENGTH = 50;
    public static final int MAX_NAME_LENGTH = 20;        // 카테고리/태그 이름
    public static final int MAX_NOTE_LENGTH = 1000;      // 테이스팅 노트 본문
}
