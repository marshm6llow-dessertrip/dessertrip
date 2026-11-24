package com.dessertrip.dessertrip_backend.dto.collect;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/** 스티커 생성 응답 (API 문서: POST /api/v1/users/me/stickers) */
public record StickerCreatedResponse(
        @JsonProperty("sticker_id") int stickerId,
        @JsonProperty("dessert_id") int dessertId,
        @JsonProperty("sticker_img_url") String stickerImgUrl,
        @JsonProperty("created_at") LocalDateTime createdAt) { }
