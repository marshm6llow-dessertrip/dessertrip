package com.dessertrip.dessertrip_backend.dto.friend;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FriendListItemResponse(
        @JsonProperty("user_id")
        Integer userId,

        String nickname,

        @JsonProperty("profile_image_url")
        String profileImageUrl,

        @JsonProperty("friend_code")
        String friendCode,

        @JsonProperty("user_level")
        int userLevel,

        @JsonProperty("user_title")
        String userTitle
) {
}