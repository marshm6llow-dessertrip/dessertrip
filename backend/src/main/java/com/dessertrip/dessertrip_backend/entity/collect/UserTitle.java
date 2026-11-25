package com.dessertrip.dessertrip_backend.entity.collect;


import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/** 유저 칭호 종류 (레벨 구간별 칭호) */
@Entity
@Table(name = "user_title")
@SQLDelete(sql = "UPDATE user_title SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class UserTitle extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "title_name", nullable = false)
    private String name;

    @Column(name = "min_level", nullable = false)
    private Integer minLevel;

    @Column(name = "max_level")
    private Integer maxLevel;

    protected UserTitle() { }

    public UserTitle(String name, int minLevel, Integer maxLevel) {
        this.name = name;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
    }

    public Integer getId() { return id; }

    public String getName() { return name; }

    public Integer getMinLevel() { return minLevel; }

    public Integer getMaxLevel() { return maxLevel; }
}
