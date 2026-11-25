package com.example.dessertrip.ui.collect.data;

import java.util.HashMap;
import java.util.Map;

/** 도감 화면 위쪽 카드에 쓰는 요약(이름, 친구 코드, 레벨, 진행도). 프로필 + 도감 통계 응답을 합쳐서 만든다. */
public class PlateDto {
    public String nickname = "";
    public String friendCode = "";
    public String bio = "";
    public int level = 1;
    public String levelTitle = "";
    public int collectedCount;
    public int goalCount = 30;
    public Map<String, Integer> categoryCounts = new HashMap<>();
}
