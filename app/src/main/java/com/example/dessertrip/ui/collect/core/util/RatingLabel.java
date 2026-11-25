package com.example.dessertrip.ui.collect.core.util;

/** 별점(0.5 단위)에 맞는 한 줄 문구 */
public final class RatingLabel {
    private RatingLabel() { }

    public static String of(float r) {
        if (r <= 0f) return "별점을 남겨주세요";
        if (r < 2.5f) return "아쉬운 맛이었어요";
        if (r < 4f) return "무난했어요";
        if (r < 5f) return "맛있었어요";
        return "또 먹고 싶은 맛이에요!";
    }
}
