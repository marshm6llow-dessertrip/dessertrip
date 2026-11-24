package com.dessertrip.dessertrip_backend.entity.collect;


import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import org.hibernate.annotations.BatchSize;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 커뮤니티 게시글. 테이스팅 노트를 "커뮤니티에 공유"하면 노트 내용을 복사해 게시글 한 개를 만든다.
 * 게시글은 그 시점의 스냅샷이다: 이후 노트를 고치거나 지워도 게시글은 바뀌지 않는다.
 */
@Entity
@Table(name = "post")
@SQLDelete(sql = "UPDATE post SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_category_id", nullable = false)
    private PostCategory postCategory;

    @Column(name = "cafe_id")
    private Integer cafeId;

    @Column(name = "dessert_id")
    private Integer dessertId;

    @Column(name = "cafe_menu_id")
    private Integer cafeMenuId;

    @Column(name = "visit_date")
    private LocalDate visitDate;

    @Column(name = "rating")
    private Double rating;

    @Column(name = "post_title", nullable = false, length = 2048)
    private String postTitle;

    @Column(name = "post_text", columnDefinition = "TEXT")
    private String postText;

    @Column(name = "sticker_id")
    private Integer stickerId;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC, id ASC")
    @BatchSize(size = 50)
    private List<PostImage> images = new ArrayList<>();

    protected Post() { }

    public Post(Integer userId, PostCategory postCategory, String postTitle) {
        this.userId = userId;
        this.postCategory = postCategory;
        this.postTitle = postTitle;
    }

    /** 테이스팅 노트(dessert)의 내용을 게시글로 복사한다. 게시글을 처음 만들 때만 쓴다. */
    public void copyFrom(Dessert d) {
        this.cafeId = d.getCafe() == null ? null : d.getCafe().getId();
        this.dessertId = d.getId();
        this.stickerId = d.getSticker().getId();
        this.visitDate = d.getVisitDate();
        this.rating = d.getRating();
        this.postText = d.getDescription();

        List<String> files = d.imageFileNames();
        images.removeIf(img -> !files.contains(img.getImageUrl()));
        for (int i = 0; i < files.size(); i++) {
            String file = files.get(i);
            PostImage existing = images.stream().filter(img -> img.getImageUrl().equals(file)).findFirst().orElse(null);
            if (existing != null) existing.setDisplayOrder(i);
            else images.add(new PostImage(this, file, i));
        }
    }

    public Integer getId() { return id; }

    public Integer getDessertId() { return dessertId; }

    public String getPostTitle() { return postTitle; }
}
