package com.example.dessertrip.ui.friend.data;

import com.google.gson.annotations.SerializedName;

public class FriendRequestDto {

    @SerializedName("receiver_id")
    public int receiverId;

    public FriendRequestDto(int receiverId) {
        this.receiverId = receiverId;
    }
}