package com.example.dessertrip.ui.collect.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * PATCH /api/v1/users/me/tasting-notes/{noteId} 요청 (multipart 의 "request" 파트, JSON). 새로 올리는 사진은 "photos" 파트로 따로 전송.
 * rating / tags / content 는 문서의 필드, 나머지는 [확장] 필드. cafeId 가 0 이면 "장소 없음".
 */
public class TastingRequest {
    public float rating;
    public List<String> tags;
    public String content;
    // ---- [확장] ----
    @SerializedName("dessert_name") public String dessertName;
    public String category;
    @SerializedName("cafe_id") public int cafeId;
    @SerializedName("visit_date") public String visitDate; // yyyy-MM-dd
    @SerializedName("share_to_community") public boolean shareToCommunity;
    @SerializedName("keep_images") public List<String> keepImages;
}
