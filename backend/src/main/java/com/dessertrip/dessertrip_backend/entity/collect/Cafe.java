package com.dessertrip.dessertrip_backend.entity.collect;


import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/** 카페(장소). 디저트 기록에서 "어디서 먹었나"로 고른다. TODO: 지도/장소 검색 API 가 확정되면 연동 */
@Entity
@Table(name = "cafe")
@SQLDelete(sql = "UPDATE cafe SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Cafe extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "cafe_name", nullable = false, length = 50)
    private String name;

    @Column(name = "cafe_latitude", nullable = false)
    private Double latitude;

    @Column(name = "cafe_longitude", nullable = false)
    private Double longitude;

    @Column(name = "cafe_address")
    private String address;

    @Column(name = "cafe_email")
    private String email;

    @Column(name = "cafe_call")
    private String call;

    @Column(name = "cafe_description")
    private String description;

    @Column(name = "cafe_img_url", length = 2048)
    private String imgUrl;

    protected Cafe() { }

    public Cafe(String name, String address, double latitude, double longitude) {
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Integer getId() { return id; }

    public String getName() { return name; }

    public String getAddress() { return address; }

    public double getLatitude() { return latitude; }

    public double getLongitude() { return longitude; }

    public String getImgUrl() { return imgUrl; }
}
