package com.example.dessertrip.ui.collect.data;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/** GET /api/v1/users/me/profile 응답 */
public class ProfileDto {
    @SerializedName("user_id") public long userId;
    @SerializedName("user_name") public String userName = "";
    @SerializedName("user_friend_code") public String userFriendCode = "";
    @SerializedName("user_description") public String userDescription = "";
    @SerializedName("user_level") public int userLevel = 1;
    @SerializedName("user_title") public String userTitle = "";
    @SerializedName("sticker_count") public int stickerCount;
    @SerializedName("record_count") public int recordCount;
    @SerializedName("friend_count") public int friendCount;
    @SerializedName("stickers_until_next_level") public int stickersUntilNextLevel;
    @SerializedName("recent_stickers") public List<RecentSticker> recentStickers = new ArrayList<>();

    public static class RecentSticker {
        @SerializedName("sticker_id") public long stickerId;
        @SerializedName("dessert_name") public String dessertName;
        @SerializedName("sticker_image_url") public String stickerImageUrl;
    }
}
