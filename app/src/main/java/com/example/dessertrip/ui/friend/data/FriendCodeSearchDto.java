package com.example.dessertrip.ui.friend.data;

import com.google.gson.annotations.SerializedName;

public class FriendCodeSearchDto {

    @SerializedName("user_id")
    public int userId;

    @SerializedName("nickname")
    public String nickname;

    @SerializedName("friend_code")
    public String friendCode;

    @SerializedName("description")
    public String description;

    @SerializedName("profile_image_url")
    public String profileImageUrl;
}