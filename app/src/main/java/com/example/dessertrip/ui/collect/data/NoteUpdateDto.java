package com.example.dessertrip.ui.collect.data;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/** PATCH /api/v1/users/me/tasting-notes/{noteId} 응답 */
public class NoteUpdateDto {
    @SerializedName("note_id") public long noteId;
    @SerializedName("dessert_id") public long dessertId;
    public Double rating;
    public List<String> tags = new ArrayList<>();
    public String content;
    public List<String> images = new ArrayList<>();
    @SerializedName("updated_at") public String updatedAt;
}
