package com.example.dessertrip.ui.friend.data;

import com.google.gson.annotations.SerializedName;

public class FriendRequestResponseDto {

    @SerializedName("request_id")
    public int requestId;

    @SerializedName("receiver_id")
    public int receiverId;

    @SerializedName("status")
    public String status;

    @SerializedName("created_at")
    public String createdAt;
}