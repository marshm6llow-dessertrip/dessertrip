package com.dessertrip.dessertrip_backend.dto.friend;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserSearchItemResponse(
        @JsonProperty("user_id")
        Integer userId,

        String nickname,

        @JsonProperty("profile_image_url")
        String profileImageUrl
) {
}
