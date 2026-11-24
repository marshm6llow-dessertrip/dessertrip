package com.example.dessertrip.ui.collect.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/** PUT /api/v1/users/me/plate/stickers 요청: 플레이트에 올릴 스티커 id 목록 (위치는 서버가 자동 배치) */
public class SavePlateRequest {
    @SerializedName("sticker_ids") public final List<Long> stickerIds;

    public SavePlateRequest(List<Long> stickerIds) { this.stickerIds = stickerIds; }
}
