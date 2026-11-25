package com.dessertrip.dessertrip_backend.entity.collect;


import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * 접시 위 스티커의 배치. position_x/y 는 접시 안 위치 비율(0~1)을 소수 둘째 자리까지 저장한다.
 * (plate_id, sticker_id) 가 UNIQUE 라서 이 테이블만은 논리 삭제 대신 실제로 지운다.
 */
@Entity
@Table(name = "plate_sticker")
@SQLRestriction("deleted_at IS NULL")
public class PlateSticker extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "plate_id", nullable = false)
    private Integer plateId;

    @Column(name = "sticker_id", nullable = false)
    private Integer stickerId;

    @Column(name = "position_x", nullable = false)
    private Double positionX;

    @Column(name = "position_y", nullable = false)
    private Double positionY;

    @Column(name = "rotation", nullable = false)
    private Double rotation = 0.0;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    protected PlateSticker() { }

    public PlateSticker(Integer plateId, Integer stickerId, double x, double y, int displayOrder) {
        this.plateId = plateId;
        this.stickerId = stickerId;
        this.positionX = x;
        this.positionY = y;
        this.displayOrder = displayOrder;
    }

    public Integer getStickerId() { return stickerId; }

    public Double getPositionX() { return positionX; }

    public Double getPositionY() { return positionY; }

    public void moveTo(double x, double y) {
        this.positionX = x;
        this.positionY = y;
    }
}
