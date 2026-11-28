package com.example.dessertrip.ui.friend.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PublicPlateDto {

    @SerializedName("user_id")
    public int userId;

    @SerializedName("user_name")
    public String userName;

    @SerializedName("user_friend_code")
    public String userFriendCode;

    @SerializedName("user_description")
    public String userDescription;

    @SerializedName("profile_image_url")
    public String profileImageUrl;

    @SerializedName("user_level")
    public int userLevel;

    @SerializedName("user_title")
    public String userTitle;

    @SerializedName("total_sticker_count")
    public int totalStickerCount;

    @SerializedName("next_level_required_count")
    public int nextLevelRequiredCount;

    @SerializedName("remaining_sticker_count")
    public int remainingStickerCount;

    @SerializedName("category_counts")
    public List<CategoryCount> categoryCounts;

    public Plate plate;

    public static class CategoryCount {

        @SerializedName("category_id")
        public int categoryId;

        @SerializedName("category_name")
        public String categoryName;

        public int count;
    }

    public static class Plate {

        @SerializedName("plate_id")
        public Integer plateId;

        public List<StickerItem> stickers;
    }

    public static class StickerItem {

        @SerializedName("sticker_id")
        public int stickerId;

        @SerializedName("sticker_name")
        public String stickerName;

        @SerializedName("sticker_img_url")
        public String stickerImgUrl;

        @SerializedName("position_x")
        public double positionX;

        @SerializedName("position_y")
        public double positionY;

        @SerializedName("acquisition_type")
        public String acquisitionType;

        public boolean isGifted() {
            return "GIFT".equals(acquisitionType);
        }
    }
}