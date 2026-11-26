package com.dessertrip.dessertrip_backend.dto.friend;

import com.dessertrip.dessertrip_backend.entity.friend.FriendRequestStatus;
import jakarta.validation.constraints.NotNull;

public record FriendRequestUpdateRequest(

        @NotNull(message = "요청 상태는 필수입니다.")
        FriendRequestStatus status

) {
}