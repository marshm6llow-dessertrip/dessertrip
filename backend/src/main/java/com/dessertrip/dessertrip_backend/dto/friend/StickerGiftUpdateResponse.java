package com.dessertrip.dessertrip_backend.dto.friend;

import com.dessertrip.dessertrip_backend.entity.friend.StickerShareStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record StickerGiftUpdateResponse(

        @JsonProperty("gift_id")
        Integer giftId,

        @JsonProperty("sender_id")
        Integer senderId,

        @JsonProperty("sticker_id")
        Integer stickerId,

        @JsonProperty("received_sticker_id")
        Integer receivedStickerId,

        StickerShareStatus status,

        @JsonProperty("processed_at")
        LocalDateTime processedAt

) {
}
