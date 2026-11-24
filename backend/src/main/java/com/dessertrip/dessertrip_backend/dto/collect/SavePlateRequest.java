package com.dessertrip.dessertrip_backend.dto.collect;

import jakarta.validation.constraints.NotNull;

import java.util.List;

/** 플레이트 스티커 저장 요청 (API 문서: PUT /api/v1/users/me/plate/stickers). 위치는 서버가 자동 배치한다. */
public record SavePlateRequest(
        @NotNull(message = "배열이어야 합니다.")
        @com.fasterxml.jackson.annotation.JsonProperty("sticker_ids")
        List<Long> stickerIds) { }
