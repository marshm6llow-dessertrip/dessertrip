package com.dessertrip.dessertrip_backend.dto.friend;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.dessertrip.dessertrip_backend.entity.friend.FriendRequestStatus;

import java.time.LocalDateTime;

public record FriendRequestCreateResponse(

        @JsonProperty("request_id")
        Integer requestId,

        @JsonProperty("receiver_id")
        Integer receiverId,

        FriendRequestStatus status,

        @JsonProperty("created_at")
        LocalDateTime createdAt

) {
}