package com.dessertrip.dessertrip_backend.entity.collect;


import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/** 디저트에 붙은 태그 (디저트 N : 태그 N) */
@Entity
@Table(name = "dessert_tag")
@SQLDelete(sql = "UPDATE dessert_tag SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class DessertTag extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dessert_id", nullable = false)
    private Dessert dessert;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    protected DessertTag() { }

    public DessertTag(Dessert dessert, Tag tag) {
        this.dessert = dessert;
        this.tag = tag;
    }

    public Integer getId() { return id; }

    public Tag getTag() { return tag; }
}
