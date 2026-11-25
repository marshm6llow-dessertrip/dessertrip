package com.dessertrip.dessertrip_backend.common.collect;

import java.util.List;

/** 접시/레벨 규칙. TODO: 레벨 기준·목표 수는 기획 확정 후 조정 */
public final class PlatePolicy {

    private PlatePolicy() { }

    public static final int MAX_PLATE_STICKERS = 8;     // 접시 위 스티커 최대 개수
    public static final int GOAL_COUNT = 30;            // 도감 수집 목표
    public static final int DESSERTS_PER_LEVEL = 10;    // 레벨이 오르는 도감 개수
    public static final List<String> LEVEL_TITLES = List.of("달콤한 입문자", "달콤한 탐험가", "디저트 수집가", "디저트 마스터");

    /** 다음 레벨이 되려면 필요한 누적 개수 (최고 레벨이면 현재 레벨의 기준 개수) */
    public static int nextLevelRequired(int level) {
        return Math.min(level, LEVEL_TITLES.size() - 1) * DESSERTS_PER_LEVEL;
    }

    /** 현재 레벨 안에서의 진행률(0~100). 최고 레벨이면 100 */
    public static int progressPercent(int level, long collected) {
        if (level >= LEVEL_TITLES.size()) return 100;
        long inLevel = collected - (long) (level - 1) * DESSERTS_PER_LEVEL;
        return (int) Math.max(0, Math.min(100, inLevel * 100 / DESSERTS_PER_LEVEL));
    }

    /** 수집한 디저트 개수로 레벨 계산 (1 ~ 칭호 개수) */
    public static int levelFor(long collected) {
        return (int) Math.min(LEVEL_TITLES.size(), 1 + collected / DESSERTS_PER_LEVEL);
    }
}
