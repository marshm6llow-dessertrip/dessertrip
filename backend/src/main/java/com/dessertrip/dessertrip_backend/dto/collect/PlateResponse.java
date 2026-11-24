package com.dessertrip.dessertrip_backend.dto.collect;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 플레이트 응답. 조회(GET /api/v1/users/me/plate)에는 sticker_img_url 이 들어가고,
 * 저장(PUT /api/v1/users/me/plate/stickers) 응답에서는 sticker_img_url 이 빠진다(null 이면 생략).
 */
public record PlateResponse(
        @JsonProperty("plate_id") int plateId,
        List<Item> stickers) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Item(
            @JsonProperty("sticker_id") int stickerId,
            @JsonProperty("sticker_img_url") String stickerImgUrl,
            @JsonProperty("position_x") double positionX,
            @JsonProperty("position_y") double positionY) { }
}
