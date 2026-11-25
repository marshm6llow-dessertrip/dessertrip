package com.dessertrip.dessertrip_backend.entity.collect;


import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * 유저가 수집한 스티커(누끼 이미지). 디저트 기록(테이스팅 노트)은 dessert 테이블이 이 스티커에 붙는다.
 * sticker_img_url 에는 저장소의 파일 이름을 넣고, 응답할 때 서버 주소를 붙여 URL 로 바꾼다.
 */
@Entity
@Table(name = "sticker")
@SQLDelete(sql = "UPDATE sticker SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Sticker extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "sticker_name", nullable = false)
    private String name;

    @Column(name = "sticker_img_url", nullable = false, length = 2048)
    private String imgUrl;

    @Column(name = "sticker_original_img_url", length = 2048)
    private String originalImgUrl;

    @Column(name = "is_present")
    private Boolean present;

    @Column(name = "source_sticker_id")
    private Integer sourceStickerId;

    protected Sticker() { }

    public Sticker(Integer userId, String name, String imgUrl) {
        this.userId = userId;
        this.name = name;
        this.imgUrl = imgUrl;
        this.present = false;
    }

    /** 선물로 받은 스티커 (is_present = true) */
    public static Sticker gift(Integer userId, String name, String imgUrl) {
        Sticker s = new Sticker(userId, name, imgUrl);
        s.present = true;
        return s;
    }

    public void rename(String name) { this.name = name; }

    public Integer getId() { return id; }

    public Integer getUserId() { return userId; }

    public String getName() { return name; }

    public String getImgUrl() { return imgUrl; }

    /** is_present: 선물로 받은 스티커인지 (선물 기능이 생기기 전까지는 항상 false) */
    public boolean isPresent() { return Boolean.TRUE.equals(present); }
}
