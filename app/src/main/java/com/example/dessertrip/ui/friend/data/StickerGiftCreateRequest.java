package com.example.dessertrip.ui.friend.data;

import com.google.gson.annotations.SerializedName;

public class StickerGiftCreateRequest {

    @SerializedName("sticker_id")
    public final int stickerId;

    @SerializedName("receiver_id")
    public final int receiverId;

    public final String message;

    public StickerGiftCreateRequest(
            int stickerId,
            int receiverId,
            String message
    ) {
        this.stickerId = stickerId;
        this.receiverId = receiverId;
        this.message = message;
    }
}