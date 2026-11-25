package com.dessertrip.dessertrip_backend.dto.collect;

import com.dessertrip.dessertrip_backend.common.collect.NamePolicy;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

/**
 * 테이스팅 노트 수정 요청 (API 문서: PATCH /api/v1/users/me/tasting-notes/{noteId}).
 * JSON 본문으로도 보내고, 사진을 올릴 때는 multipart 의 "request" 파트(JSON)로도 보낸다.
 * PATCH 이므로 null 인 필드는 바꾸지 않는다. 문서의 rating / tags / content 외에는 [확장] 필드다.
 */
public record TastingNoteRequest(
        @DecimalMin(value = "0.5", message = "0.5~5.0 사이여야 합니다.")
        @DecimalMax(value = "5.0", message = "0.5~5.0 사이여야 합니다.")
        Double rating,

        List<String> tags,

        @Size(max = NamePolicy.MAX_NOTE_LENGTH, message = "1,000자 이하여야 합니다.")
        String content,

        // ---- [확장] ----
        @JsonProperty("dessert_name")
        @Size(max = NamePolicy.MAX_DESSERT_NAME_LENGTH, message = "50자 이하여야 합니다.")
        String dessertName,

        String category,

        @JsonProperty("cafe_id") Integer cafeId,

        @JsonProperty("visit_date") LocalDate visitDate,

        @JsonProperty("share_to_community") Boolean shareToCommunity,

        /** 수정 시 그대로 유지할 기존 사진 URL. null 이면 사진은 그대로 두고, 목록을 주면 거기 없는 기존 사진은 삭제된다. */
        @JsonProperty("keep_images") List<String> keepImages) { }
