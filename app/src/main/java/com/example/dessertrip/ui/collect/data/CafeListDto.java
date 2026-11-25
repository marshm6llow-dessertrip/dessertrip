package com.example.dessertrip.ui.collect.data;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/** GET /api/v1/cafes 응답 */
public class CafeListDto {
    public List<Item> cafes = new ArrayList<>();
    @SerializedName("total_count") public int totalCount;

    public static class Item {
        @SerializedName("cafe_id") public long cafeId;
        @SerializedName("cafe_name") public String cafeName = "";
        public Double latitude;
        public Double longitude;
        public String address = "";
        @SerializedName("thumbnail_url") public String thumbnailUrl;
        @SerializedName("is_bookmarked") public boolean bookmarked;
    }
}
