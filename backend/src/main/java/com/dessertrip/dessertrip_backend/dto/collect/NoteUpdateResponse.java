package com.dessertrip.dessertrip_backend.dto.collect;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

/** 테이스팅 노트 수정 응답 (API 문서: PATCH /api/v1/users/me/tasting-notes/{noteId}) */
public record NoteUpdateResponse(
        @JsonProperty("note_id") int noteId,
        @JsonProperty("dessert_id") int dessertId,
        Double rating,
        List<String> tags,
        String content,
        List<String> images,
        @JsonProperty("updated_at") LocalDateTime updatedAt) { }
