package com.dessertrip.dessertrip_backend.dto.friend;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record StickerRequestCreateRequest(
        @JsonProperty("sticker_id")
        @NotNull
        @Positive
        Integer stickerId
) {
}