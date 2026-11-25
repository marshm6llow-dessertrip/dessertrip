package com.dessertrip.dessertrip_backend.entity.collect;


import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/** 디저트 카테고리. 기본값(케이크/빵/구움과자) 외에 사용자가 추가할 수 있다. */
@Entity
@Table(name = "dessert_category")
@SQLDelete(sql = "UPDATE dessert_category SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class DessertCategory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "category_name", nullable = false)
    private String name;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "is_active")
    private Boolean active = true;

    protected DessertCategory() { }

    public DessertCategory(String name, int displayOrder) {
        this.name = name;
        this.displayOrder = displayOrder;
    }

    /** 목록·통계·추천에 나오지 않게 숨긴다 (이미 붙어 있는 디저트에는 이름이 그대로 보인다) */
    public void deactivate() { this.active = false; }

    public Integer getId() { return id; }

    public String getName() { return name; }

    public Integer getDisplayOrder() { return displayOrder; }

    public Boolean getActive() { return active; }
}
