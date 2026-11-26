package com.dessertrip.dessertrip_backend.dto.friend;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FriendDeleteResponse(
        @JsonProperty("friend_id")
        Integer friendId,

        @JsonProperty("is_friend")
        boolean isFriend,

        @JsonProperty("friend_count")
        long friendCount,

        String message
) {
}