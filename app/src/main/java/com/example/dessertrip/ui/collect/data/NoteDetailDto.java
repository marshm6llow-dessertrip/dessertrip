package com.example.dessertrip.ui.collect.data;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/** GET /api/v1/users/me/tasting-notes/{noteId} 응답. cafeId 이하는 문서에 없는 [확장] 필드. */
public class NoteDetailDto {
    @SerializedName("note_id") public long noteId;
    public Dessert dessert = new Dessert();
    public Double rating;
    public List<String> tags = new ArrayList<>();
    public String content;
    public List<String> images = new ArrayList<>();
    @SerializedName("created_at") public String createdAt;
    // ---- [확장] ----
    @SerializedName("cafe_id") public Integer cafeId;
    @SerializedName("cafe_name") public String cafeName;
    @SerializedName("visit_date") public String visitDate;
    @SerializedName("share_to_community") public boolean shareToCommunity;
    @SerializedName("collection_number") public int collectionNumber;

    public static class Dessert {
        @SerializedName("dessert_id") public long dessertId;
        @SerializedName("dessert_name") public String dessertName = "";
        @SerializedName("dessert_image_url") public String dessertImageUrl;
        public String description;
        public String category;
    }
}
