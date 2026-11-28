package com.dessertrip.dessertrip_backend.dto.friend;

import com.dessertrip.dessertrip_backend.entity.friend.StickerRequestStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record StickerRequestUpdateResponse(
        @JsonProperty("request_id")
        Integer requestId,

        @JsonProperty("requester_id")
        Integer requesterId,

        @JsonProperty("sticker_id")
        Integer stickerId,

        @JsonProperty("received_sticker_id")
        Integer receivedStickerId,

        StickerRequestStatus status,

        @JsonProperty("processed_at")
        LocalDateTime processedAt
) {
}