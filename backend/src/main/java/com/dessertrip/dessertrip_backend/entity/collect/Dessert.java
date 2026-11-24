package com.dessertrip.dessertrip_backend.entity.collect;


import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import org.hibernate.annotations.BatchSize;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 유저 개인의 디저트 기록 = 테이스팅 노트. 스티커를 만들 때 함께 만들어지고(이름·카테고리는 그 직후 저장),
 * 별점·방문일·노트·사진·태그는 테이스팅 노트를 쓰면 채워진다.
 */
@Entity
@Table(name = "dessert")
@SQLDelete(sql = "UPDATE dessert SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Dessert extends BaseTimeEntity {

    /** 스티커를 만든 직후, 이름을 입력하기 전의 임시 이름 (응답에서는 빈 문자열로 내보낸다) */
    public static final String UNNAMED = "이름 없는 디저트";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cafe_id")
    private Cafe cafe;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sticker_id", nullable = false)
    private Sticker sticker;

    @Column(name = "dessert_name", nullable = false)
    private String name;

    @Column(name = "dessert_description", length = 2048)
    private String description;       // 테이스팅 노트 본문

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dessert_category_id")
    private DessertCategory category;

    @Column(name = "visit_date")
    private LocalDate visitDate;

    @Column(name = "rating")
    private Double rating;             // 0.5 단위. null 이면 테이스팅 노트 미작성

    @OneToMany(mappedBy = "dessert", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC, id ASC")
    @BatchSize(size = 50)
    private List<DessertImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "dessert", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    @BatchSize(size = 50)
    private List<DessertTag> dessertTags = new ArrayList<>();

    protected Dessert() { }

    public Dessert(Integer userId, Sticker sticker, String name) {
        this.userId = userId;
        this.sticker = sticker;
        this.name = name;
    }

    /** 스티커 완성 직후: 필수 정보(이름, 카테고리) 저장 */
    public void updateBasics(String name, DessertCategory category) {
        this.name = name;
        this.category = category;
        this.sticker.rename(name);
    }

    /** 테이스팅 노트 저장/수정 (사진·태그는 replaceImages / replaceTags 로 따로 바꾼다) */
    public void updateTastingNote(String name, DessertCategory category, Cafe cafe, Double rating,
                                  LocalDate visitDate, String note) {
        updateBasics(name, category);
        this.cafe = cafe;
        this.rating = rating;
        this.visitDate = visitDate;
        this.description = note;
    }

    /** 사진을 주어진 파일 이름 순서로 맞춘다: 빠진 것은 삭제, 새 것은 추가, 남는 것은 순서만 갱신 */
    public void replaceImages(List<String> fileNames) {
        images.removeIf(img -> !fileNames.contains(img.getImageUrl()));
        for (int i = 0; i < fileNames.size(); i++) {
            String file = fileNames.get(i);
            DessertImage existing = images.stream().filter(img -> img.getImageUrl().equals(file)).findFirst().orElse(null);
            if (existing != null) existing.setDisplayOrder(i);
            else images.add(new DessertImage(this, file, i));
        }
    }

    public void replaceTags(List<Tag> tags) {
        dessertTags.removeIf(dt -> !tags.contains(dt.getTag()));
        for (Tag tag : tags) {
            boolean has = dessertTags.stream().anyMatch(dt -> dt.getTag().equals(tag));
            if (!has) dessertTags.add(new DessertTag(this, tag));
        }
    }

    public List<String> imageFileNames() { return images.stream().map(DessertImage::getImageUrl).toList(); }

    public List<Tag> tags() { return dessertTags.stream().map(DessertTag::getTag).toList(); }

    public Integer getId() { return id; }

    public Integer getUserId() { return userId; }

    public Cafe getCafe() { return cafe; }

    public Sticker getSticker() { return sticker; }

    public String getName() { return name; }

    public String getDescription() { return description; }

    public DessertCategory getCategory() { return category; }

    public LocalDate getVisitDate() { return visitDate; }

    public Double getRating() { return rating; }
}
