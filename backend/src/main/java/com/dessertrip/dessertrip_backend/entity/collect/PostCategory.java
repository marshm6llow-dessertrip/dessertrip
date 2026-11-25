package com.dessertrip.dessertrip_backend.entity.collect;


import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/** 게시글 종류 (게시글 / 리뷰 / 테이스팅 노트) */
@Entity
@Table(name = "post_category")
@SQLDelete(sql = "UPDATE post_category SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class PostCategory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "category_name", nullable = false)
    private String name;

    @Column(name = "category_description")
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    protected PostCategory() { }

    public PostCategory(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Integer getId() { return id; }

    public String getName() { return name; }
}
