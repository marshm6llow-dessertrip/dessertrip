package com.dessertrip.dessertrip_backend.dto.friend;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record PublicPlateResponse(
        @JsonProperty("user_id") int userId,
        @JsonProperty("user_name") String userName,
        @JsonProperty("user_friend_code") String userFriendCode,
        @JsonProperty("user_description") String userDescription,
        @JsonProperty("profile_image_url") String profileImageUrl,
        @JsonProperty("user_level") int userLevel,
        @JsonProperty("user_title") String userTitle,
        @JsonProperty("total_sticker_count") int totalStickerCount,
        @JsonProperty("next_level_required_count") int nextLevelRequiredCount,
        @JsonProperty("remaining_sticker_count") int remainingStickerCount,
        @JsonProperty("category_counts") List<CategoryCount> categoryCounts,
        Plate plate
) {

    public record CategoryCount(
            @JsonProperty("category_id") int categoryId,
            @JsonProperty("category_name") String categoryName,
            int count
    ) {}

    public record Plate(
            @JsonProperty("plate_id") Integer plateId,
            List<StickerItem> stickers
    ) {}

    public record StickerItem(
            @JsonProperty("sticker_id") int stickerId,
            @JsonProperty("sticker_name") String stickerName,
            @JsonProperty("sticker_img_url") String stickerImgUrl,
            @JsonProperty("position_x") double positionX,
            @JsonProperty("position_y") double positionY,
            @JsonProperty("acquisition_type") String acquisitionType
    ) {}
}