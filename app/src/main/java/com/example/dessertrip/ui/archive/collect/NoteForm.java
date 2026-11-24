package com.example.dessertrip.ui.archive.collect;

import android.net.Uri;

import com.example.dessertrip.ui.collect.data.PlaceDto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** 테이스팅 노트 입력값 */
public class NoteForm {
    public LocalDate tastedDate = LocalDate.now();
    public float rating = 0f;
    public String name = "";
    public PlaceDto place;
    public String category;
    public Set<String> tags = new LinkedHashSet<>();
    public String note = "";
    public List<String> existingPhotoUrls = new ArrayList<>();
    public List<Uri> newPhotos = new ArrayList<>();
    public boolean share = true;

    public int photoCount() { return existingPhotoUrls.size() + newPhotos.size(); }
}
