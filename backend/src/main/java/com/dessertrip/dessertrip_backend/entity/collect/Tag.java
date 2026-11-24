package com.dessertrip.dessertrip_backend.entity.collect;


import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/** 맛/식감 태그. 사용자가 새로 추가하면 태그 목록에 저장된다. */
@Entity
@Table(name = "tag")
@SQLDelete(sql = "UPDATE tag SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Tag extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "tag_name", nullable = false)
    private String name;

    protected Tag() { }

    public Tag(String name) {
        this.name = name;
    }

    public Integer getId() { return id; }

    public String getName() { return name; }
}
