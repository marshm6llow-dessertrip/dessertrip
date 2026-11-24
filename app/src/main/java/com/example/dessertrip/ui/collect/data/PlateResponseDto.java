package com.example.dessertrip.ui.collect.data;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/** GET /api/v1/users/me/plate, PUT /api/v1/users/me/plate/stickers 응답 (저장 응답에는 sticker_img_url 이 없다) */
public class PlateResponseDto {
    @SerializedName("plate_id") public long plateId;
    public List<Item> stickers = new ArrayList<>();

    public static class Item {
        @SerializedName("sticker_id") public long stickerId;
        @SerializedName("sticker_img_url") public String stickerImgUrl;
        @SerializedName("position_x") public float positionX;
        @SerializedName("position_y") public float positionY;
    }
}
