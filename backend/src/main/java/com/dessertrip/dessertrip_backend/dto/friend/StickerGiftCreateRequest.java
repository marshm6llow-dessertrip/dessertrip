package com.dessertrip.dessertrip_backend.dto.friend;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record StickerGiftCreateRequest(

        @JsonProperty("sticker_id")
        @NotNull
        @Positive
        Integer stickerId,

        @JsonProperty("receiver_id")
        @NotNull
        @Positive
        Integer receiverId,

        @Size(max = 200)
        String message

) {
}
