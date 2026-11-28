package com.example.dessertrip.ui.friend.data;

import com.google.gson.annotations.SerializedName;

public class StickerGiftCreateResponse {

    @SerializedName("gift_id")
    public int giftId;

    @SerializedName("sticker_id")
    public int stickerId;

    @SerializedName("receiver_id")
    public int receiverId;

    public String message;

    public String status;

    @SerializedName("created_at")
    public String createdAt;
}