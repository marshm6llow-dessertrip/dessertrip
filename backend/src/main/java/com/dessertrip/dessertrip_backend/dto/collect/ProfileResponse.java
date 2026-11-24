package com.dessertrip.dessertrip_backend.dto.collect;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** 내 프로필 응답 (API 문서: GET /api/v1/users/me/profile). 친구 기능이 없어서 friend_count 는 0 이다. */
public record ProfileResponse(
        @JsonProperty("user_id") int userId,
        @JsonProperty("user_name") String userName,
        @JsonProperty("user_friend_code") String userFriendCode,
        @JsonProperty("user_description") String userDescription,
        @JsonProperty("user_level") int userLevel,
        @JsonProperty("user_title") String userTitle,
        @JsonProperty("sticker_count") int stickerCount,
        @JsonProperty("record_count") int recordCount,
        @JsonProperty("friend_count") int friendCount,
        @JsonProperty("stickers_until_next_level") int stickersUntilNextLevel,
        @JsonProperty("recent_stickers") List<RecentSticker> recentStickers) {

    public record RecentSticker(
            @JsonProperty("sticker_id") int stickerId,
            @JsonProperty("dessert_name") String dessertName,
            @JsonProperty("sticker_image_url") String stickerImageUrl) { }
}
