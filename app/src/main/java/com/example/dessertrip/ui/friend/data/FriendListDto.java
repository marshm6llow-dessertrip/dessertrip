package com.example.dessertrip.ui.friend.data;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class FriendListDto {

    public List<FriendItem> friends;

    public static class FriendItem {

        @SerializedName("user_id")
        public int userId;

        public String nickname;

        @SerializedName("profile_image_url")
        public String profileImageUrl;

        @SerializedName("friend_code")
        public String friendCode;

        @SerializedName("user_level")
        public int userLevel;

        @SerializedName("user_title")
        public String userTitle;
    }
}