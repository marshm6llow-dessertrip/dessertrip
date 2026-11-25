package com.dessertrip.dessertrip_backend.entity.collect;


import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/** 유저의 플레이트(접시). 여러 개를 가질 수 있어 plate_order 로 순서를 둔다. */
@Entity
@Table(name = "dessert_plate")
@SQLDelete(sql = "UPDATE dessert_plate SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class DessertPlate extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "plate_order", nullable = false)
    private Integer plateOrder;

    protected DessertPlate() { }

    public DessertPlate(Integer userId, int plateOrder) {
        this.userId = userId;
        this.plateOrder = plateOrder;
    }

    public Integer getId() { return id; }
}
