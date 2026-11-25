package com.dessertrip.dessertrip_backend.entity.collect;


import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/** 테이스팅 노트 사진. display_order 로 순서를 관리한다. image_url 에는 저장소의 파일 이름을 넣는다. */
@Entity
@Table(name = "dessert_image")
@SQLDelete(sql = "UPDATE dessert_image SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class DessertImage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dessert_id", nullable = false)
    private Dessert dessert;

    @Column(name = "image_url", nullable = false, length = 2048)
    private String imageUrl;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    protected DessertImage() { }

    public DessertImage(Dessert dessert, String imageUrl, int displayOrder) {
        this.dessert = dessert;
        this.imageUrl = imageUrl;
        this.displayOrder = displayOrder;
    }

    void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }

    public Integer getId() { return id; }

    public String getImageUrl() { return imageUrl; }

    public Integer getDisplayOrder() { return displayOrder; }
}
