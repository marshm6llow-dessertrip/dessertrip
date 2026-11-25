package com.example.dessertrip.ui.collect.data;

import com.google.gson.annotations.SerializedName;

/** POST /api/v1/users/me/stickers 응답 */
public class StickerCreatedDto {
    @SerializedName("sticker_id") public long stickerId;
    @SerializedName("dessert_id") public long dessertId;
    @SerializedName("sticker_img_url") public String stickerImgUrl;
    @SerializedName("created_at") public String createdAt;
}
