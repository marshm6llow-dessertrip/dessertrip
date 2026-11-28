package com.dessertrip.dessertrip_backend.dto.friend;

import com.dessertrip.dessertrip_backend.entity.friend.StickerRequestStatus;
import jakarta.validation.constraints.NotNull;

public record StickerRequestUpdateRequest(
        @NotNull
        StickerRequestStatus status
) {
}