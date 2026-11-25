package com.dessertrip.dessertrip_backend.dto.collect;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 내 도감 목록 응답 (API 문서: GET /api/v1/users/me/collection).
 * [확장] 문서에 없는 필드 3개를 덧붙였다 — 앱이 카드를 그리는 데 필요해서 팀 합의가 필요하다:
 *  dessert_category_name, has_tasting_note, collection_number
 */
public record CollectionResponse(List<Item> stickers) {

    public record Item(
            @JsonProperty("sticker_id") int stickerId,
            @JsonProperty("dessert_id") int dessertId,
            @JsonProperty("dessert_name") String dessertName,
            @JsonProperty("dessert_category_id") Integer dessertCategoryId,
            @JsonProperty("sticker_img_url") String stickerImgUrl,
            @JsonProperty("cafe_name") String cafeName,
            @JsonProperty("acquisition_type") String acquisitionType,
            @JsonProperty("created_at") LocalDateTime createdAt,
            // ---- [확장] ----
            @JsonProperty("dessert_category_name") String dessertCategoryName,
            @JsonProperty("has_tasting_note") boolean hasTastingNote,
            @JsonProperty("collection_number") int collectionNumber) { }
}
