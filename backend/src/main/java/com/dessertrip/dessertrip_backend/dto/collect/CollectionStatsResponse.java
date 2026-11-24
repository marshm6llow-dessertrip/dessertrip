package com.dessertrip.dessertrip_backend.dto.collect;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** 내 도감 통계 응답 (API 문서: GET /api/v1/users/me/collection/stats) */
public record CollectionStatsResponse(
        @JsonProperty("user_level") int userLevel,
        @JsonProperty("user_title") String userTitle,
        @JsonProperty("total_sticker_count") int totalStickerCount,
        @JsonProperty("next_level_required_count") int nextLevelRequiredCount,
        @JsonProperty("remaining_sticker_count") int remainingStickerCount,
        @JsonProperty("level_progress_percent") int levelProgressPercent,
        @JsonProperty("category_counts") List<CategoryCount> categoryCounts) {

    public record CategoryCount(
            @JsonProperty("category_id") int categoryId,
            @JsonProperty("category_name") String categoryName,
            int count) { }
}
