package com.dessertrip.dessertrip_backend.dto.friend;

import com.dessertrip.dessertrip_backend.entity.friend.StickerShareStatus;
import jakarta.validation.constraints.NotNull;

public record StickerGiftUpdateRequest(

        @NotNull
        StickerShareStatus status

) {
}
