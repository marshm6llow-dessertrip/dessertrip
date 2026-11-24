package com.dessertrip.dessertrip_backend.dto.collect;

import com.dessertrip.dessertrip_backend.entity.collect.Cafe;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** 카페 목록 응답 (API 문서: GET /api/v1/cafes) */
public record CafeListResponse(
        List<Item> cafes,
        @JsonProperty("total_count") int totalCount) {

    public record Item(
            @JsonProperty("cafe_id") int cafeId,
            @JsonProperty("cafe_name") String cafeName,
            double latitude,
            double longitude,
            String address,
            @JsonProperty("thumbnail_url") String thumbnailUrl,
            @JsonProperty("is_bookmarked") boolean bookmarked) {

        public static Item from(Cafe c) {
            return new Item(c.getId(), c.getName(), c.getLatitude(), c.getLongitude(),
                    c.getAddress() == null ? "" : c.getAddress(), c.getImgUrl(), false);
        }
    }
}
