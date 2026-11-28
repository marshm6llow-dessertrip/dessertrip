package com.dessertrip.dessertrip_backend.dto.friend;

import com.dessertrip.dessertrip_backend.entity.friend.StickerRequestStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record StickerRequestCreateResponse(
        @JsonProperty("request_id")
        Integer requestId,

        @JsonProperty("sticker_id")
        Integer stickerId,

        @JsonProperty("request_receiver_id")
        Integer requestReceiverId,

        StickerRequestStatus status,

        @JsonProperty("created_at")
        LocalDateTime createdAt
) {
}