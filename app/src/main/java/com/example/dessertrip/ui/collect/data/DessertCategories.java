package com.example.dessertrip.ui.collect.data;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 카테고리는 서버가 관리하는 "이름" 목록이다 (사용자가 새로 추가할 수 있음).
 * 아래 기본값은 서버가 처음부터 갖고 있는 카테고리와 같아야 하며, 목록 화면의 칩/레벨 카드에서 자리 채우기용으로만 쓴다.
 */
public final class DessertCategories {
    private DessertCategories() { }

    public static final List<String> DEFAULTS = Collections.unmodifiableList(Arrays.asList("케이크", "빵", "구움과자"));
}
