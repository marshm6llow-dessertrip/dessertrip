package com.example.dessertrip.ui.collect.core.image;

import java.nio.FloatBuffer;

/**
 * 배경 제거 결과를 스티커로 다듬는 순수 계산 모음. Android 클래스를 쓰지 않아 JVM 단위 테스트가 가능하다.
 * (비트맵 입출력은 {@link BitmapLoader}, ML Kit 호출은 {@link BackgroundRemover} 가 맡는다.)
 */
public final class StickerMath {
    private StickerMath() { }

    /** 신뢰도가 이 값 이하면 완전 투명, 이 값 이상이면 완전 불투명. 사이는 부드럽게 이어 가장자리의 계단 현상을 줄인다. */
    static final float EDGE_LOW = 0.35f;
    static final float EDGE_HIGH = 0.75f;

    /** 전경 신뢰도(0.0~1.0) → 알파(0~255) */
    public static int alphaFor(float confidence) {
        if (confidence <= EDGE_LOW) return 0;
        if (confidence >= EDGE_HIGH) return 255;
        float t = (confidence - EDGE_LOW) / (EDGE_HIGH - EDGE_LOW);
        float smooth = t * t * (3f - 2f * t);            // smoothstep
        return Math.round(smooth * 255f);
    }

    /**
     * 원본 픽셀(ARGB)에 전경 마스크를 적용한다(배열을 제자리에서 수정).
     * 마스크는 픽셀과 같은 크기(너비×높이)여야 한다.
     *
     * @return 알파가 1 이상으로 남은(= 보이는) 픽셀 수
     */
    public static int applyMask(int[] pixels, FloatBuffer mask) {
        if (mask.capacity() < pixels.length) {
            throw new IllegalArgumentException("마스크 크기(" + mask.capacity() + ")가 이미지 크기(" + pixels.length + ")보다 작아요.");
        }
        int visible = 0;
        for (int i = 0; i < pixels.length; i++) {
            int a = alphaFor(mask.get(i));
            if (a > 0) visible++;
            pixels[i] = (pixels[i] & 0x00FFFFFF) | (a << 24);
        }
        return visible;
    }

    /**
     * 알파가 minAlpha 이상인 픽셀을 모두 포함하는 최소 사각형. 없으면 null.
     *
     * @return {left, top, right, bottom} (right, bottom 은 포함하지 않는 경계)
     */
    public static int[] findBounds(int[] argb, int width, int height, int minAlpha) {
        int left = width, top = height, right = -1, bottom = -1;
        for (int y = 0; y < height; y++) {
            int row = y * width;
            for (int x = 0; x < width; x++) {
                if ((argb[row + x] >>> 24) >= minAlpha) {
                    if (x < left) left = x;
                    if (x > right) right = x;
                    if (y < top) top = y;
                    bottom = y;
                }
            }
        }
        if (right < 0) return null;
        return new int[]{left, top, right + 1, bottom + 1};
    }

    /** 사각형을 사방으로 padding 만큼 넓히되 이미지 밖으로 나가지 않게 자른다. */
    public static int[] pad(int[] b, int width, int height, int padding) {
        return new int[]{
                Math.max(0, b[0] - padding), Math.max(0, b[1] - padding),
                Math.min(width, b[2] + padding), Math.min(height, b[3] + padding)};
    }

    /** 긴 변이 maxEdge 를 넘으면 비율을 유지해 줄인 {너비, 높이}. 이미 작으면 그대로(확대하지 않는다). */
    public static int[] fitWithin(int width, int height, int maxEdge) {
        int longest = Math.max(width, height);
        if (longest <= maxEdge) return new int[]{width, height};
        float scale = (float) maxEdge / longest;
        return new int[]{Math.max(1, Math.round(width * scale)), Math.max(1, Math.round(height * scale))};
    }

    /** BitmapFactory.inSampleSize: 디코딩 결과의 긴 변이 maxEdge 이상으로 남는 가장 큰 2의 거듭제곱 */
    public static int sampleSize(int width, int height, int maxEdge) {
        int longest = Math.max(width, height);
        int sample = 1;
        while (longest / (sample * 2) >= maxEdge) sample *= 2;
        return sample;
    }
}
