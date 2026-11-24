package com.dessertrip.dessertrip_backend.dto.collect;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 테이스팅 노트 상세 응답 (API 문서: GET /api/v1/users/me/tasting-notes/{noteId}).
 * note_id 는 dessert 행의 id 와 같다(노트가 dessert 행에 저장되므로).
 * [확장] 문서에 없는 필드: cafe_id, cafe_name, visit_date, share_to_community, collection_number
 */
public record NoteDetailResponse(
        @JsonProperty("note_id") int noteId,
        Dessert dessert,
        Double rating,
        List<String> tags,
        String content,
        List<String> images,
        @JsonProperty("created_at") LocalDateTime createdAt,
        // ---- [확장] ----
        @JsonProperty("cafe_id") Integer cafeId,
        @JsonProperty("cafe_name") String cafeName,
        @JsonProperty("visit_date") LocalDate visitDate,
        @JsonProperty("share_to_community") boolean shareToCommunity,
        @JsonProperty("collection_number") int collectionNumber) {

    public record Dessert(
            @JsonProperty("dessert_id") int dessertId,
            @JsonProperty("dessert_name") String dessertName,
            @JsonProperty("dessert_image_url") String dessertImageUrl,
            String description,
            String category) { }
}
