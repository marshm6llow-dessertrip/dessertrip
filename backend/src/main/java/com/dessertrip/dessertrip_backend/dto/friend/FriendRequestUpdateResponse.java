package com.dessertrip.dessertrip_backend.dto.friend;

import com.dessertrip.dessertrip_backend.entity.friend.FriendRequestStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record FriendRequestUpdateResponse(

        @JsonProperty("request_id")
        Integer requestId,

        FriendRequestStatus status,

        @JsonProperty("sender_id")
        Integer senderId,

        @JsonProperty("is_friend")
        boolean isFriend,

        @JsonProperty("processed_at")
        LocalDateTime processedAt

) {
}