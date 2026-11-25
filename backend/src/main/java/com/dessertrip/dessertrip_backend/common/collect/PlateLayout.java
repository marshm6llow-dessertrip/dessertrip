package com.dessertrip.dessertrip_backend.common.collect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * 접시 위 스티커의 무작위 배치. 좌표는 0~1 비율(소수 둘째 자리)이고 (0.5, 0.5)가 접시 중앙이다.
 * 자리는 원형으로 고르게 벌려 놓고(1개면 가운데, 2~6개면 한 줄 원형, 7개 이상이면 가운데 1개 + 바깥 원형),
 * 원을 무작위 각도로 돌리고 · 어느 스티커가 어느 자리에 갈지 섞고 · 자리마다 조금씩 흔들어서 매번 다른 모양이 된다.
 */
public final class PlateLayout {

    private PlateLayout() { }

    private static final double JITTER = 0.03;   // 자리마다 흔들어 주는 최대 폭
    private static final double MIN = 0.12, MAX = 0.88;   // 접시 밖으로 나가지 않게 막는 범위

    /** n 개의 스티커 자리를 무작위로 만든다. 결과의 i 번째가 i 번째 스티커의 위치다. */
    public static List<double[]> random(int n, Random rnd) {
        List<double[]> spots = new ArrayList<>();
        if (n <= 0) return spots;
        if (n == 1) {
            spots.add(jitter(0.5, 0.5, rnd));
            return spots;
        }
        boolean center = n > 6;
        int ring = center ? n - 1 : n;
        double radius = center ? 0.32 : 0.26;
        double turn = rnd.nextDouble() * 2 * Math.PI;
        if (center) spots.add(new double[]{0.5, 0.5});
        for (int i = 0; i < ring; i++) {
            double angle = 2 * Math.PI * i / ring + turn;
            spots.add(new double[]{0.5 + radius * Math.cos(angle), 0.5 + radius * Math.sin(angle)});
        }
        Collections.shuffle(spots, rnd);
        List<double[]> result = new ArrayList<>();
        for (double[] s : spots) result.add(jitter(s[0], s[1], rnd));
        return result;
    }

    private static double[] jitter(double x, double y, Random rnd) {
        return new double[]{
                round2(clamp(x + (rnd.nextDouble() * 2 - 1) * JITTER)),
                round2(clamp(y + (rnd.nextDouble() * 2 - 1) * JITTER))};
    }

    private static double clamp(double v) { return Math.max(MIN, Math.min(MAX, v)); }

    private static double round2(double v) { return Math.round(v * 100.0) / 100.0; }
}
