package com.dessertrip.dessertrip_backend.dto.friend;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FriendCodeSearchResponse(

        @JsonProperty("user_id")
        Integer userId,

        String nickname,

        @JsonProperty("friend_code")
        String friendCode,

        String description,

        @JsonProperty("profile_image_url")
        String profileImageUrl

) {
}