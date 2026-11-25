package com.example.dessertrip.ui.collect.data;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/** GET /api/v1/users/me/collection/stats 응답 */
public class StatsDto {
    @SerializedName("user_level") public int userLevel = 1;
    @SerializedName("user_title") public String userTitle = "";
    @SerializedName("total_sticker_count") public int totalStickerCount;
    @SerializedName("next_level_required_count") public int nextLevelRequiredCount;
    @SerializedName("remaining_sticker_count") public int remainingStickerCount;
    @SerializedName("level_progress_percent") public int levelProgressPercent;
    @SerializedName("category_counts") public List<CategoryCount> categoryCounts = new ArrayList<>();

    public static class CategoryCount {
        @SerializedName("category_id") public int categoryId;
        @SerializedName("category_name") public String categoryName;
        public int count;
    }
}
