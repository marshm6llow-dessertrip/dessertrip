package com.example.dessertrip.ui.collect.data;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/** GET /api/v1/users/me/collection 응답. dessertCategoryName / hasTastingNote / collectionNumber 는 문서에 없는 [확장] 필드. */
public class CollectionDto {
    public List<Item> stickers = new ArrayList<>();

    public static class Item {
        @SerializedName("sticker_id") public long stickerId;
        @SerializedName("dessert_id") public long dessertId;
        @SerializedName("dessert_name") public String dessertName;
        @SerializedName("dessert_category_id") public Integer dessertCategoryId;
        @SerializedName("sticker_img_url") public String stickerImgUrl;
        @SerializedName("cafe_name") public String cafeName;
        @SerializedName("acquisition_type") public String acquisitionType;   // DIRECT | GIFT
        @SerializedName("created_at") public String createdAt;
        // ---- [확장] ----
        @SerializedName("dessert_category_name") public String dessertCategoryName;
        @SerializedName("has_tasting_note") public boolean hasTastingNote;
        @SerializedName("collection_number") public int collectionNumber;
    }
}
