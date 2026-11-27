package com.example.dessertrip.ui.collect.data;

import java.util.ArrayList;
import java.util.List;

/** 도감 항목(화면용 모델). id 는 노트 id(= dessert id). 서버 응답(CollectionDto / NoteDetailDto / StickerCreatedDto)을 DessertRepository 가 이 모델로 바꿔 준다. */
public class ArchiveItemDto {
    public long id;
    /** 개별 스티커 id (플레이트에 올릴 때 사용. 같은 디저트를 두 번 모으면 서로 다른 값) */
    public long stickerId;
    /** 선물로 받은 스티커인지 */
    public boolean gift;
    public int number;
    public String name = "";
    public String stickerUrl;
    public String category;
    public String placeId;
    public String placeName;
    public Double latitude;
    public Double longitude;
    public Float rating;
    public String note;
    public List<String> tags = new ArrayList<>();
    public List<String> photoUrls = new ArrayList<>();
    public String tastedDate;
    public boolean shareToCommunity;
    /** 목록 응답은 노트 내용을 주지 않으므로, 노트가 있는지는 서버가 알려준 값을 쓴다 (null 이면 아래 내용으로 판단) */
    public Boolean hasTastingNote;

    public boolean hasNote() { if (hasTastingNote != null) return hasTastingNote; return rating != null || (note != null && !note.trim().isEmpty()); }
    public List<String> tagsSafe() { return tags == null ? new ArrayList<String>() : tags; }
    public List<String> photosSafe() { return photoUrls == null ? new ArrayList<String>() : photoUrls; }
}
