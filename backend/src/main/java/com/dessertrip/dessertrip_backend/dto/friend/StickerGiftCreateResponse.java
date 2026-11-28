package com.dessertrip.dessertrip_backend.dto.friend;

import com.dessertrip.dessertrip_backend.entity.friend.StickerShareStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record StickerGiftCreateResponse(

        @JsonProperty("gift_id")
        Integer giftId,

        @JsonProperty("sticker_id")
        Integer stickerId,

        @JsonProperty("receiver_id")
        Integer receiverId,

        String message,

        StickerShareStatus status,

        @JsonProperty("created_at")
        LocalDateTime createdAt

) {
}
