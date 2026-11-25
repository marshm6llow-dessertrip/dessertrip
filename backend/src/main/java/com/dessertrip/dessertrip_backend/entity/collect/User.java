package com.dessertrip.dessertrip_backend.entity.collect;


import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * 유저. 도감/접시 화면의 이름(user_name)과 태그(user_friend_code)가 여기서 온다.
 * 비밀번호는 해시값만 저장한다. 로그인/회원가입은 서버(인증) 담당자 몫이다.
 */
@Entity
@Table(name = "`user`")
@SQLDelete(sql = "UPDATE `user` SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_friend_code", nullable = false)
    private String friendCode;

    @Column(name = "user_email", nullable = false)
    private String email;

    @Column(name = "user_password", nullable = false)
    private String password;

    @Column(name = "user_name", nullable = false, length = 50)
    private String name;

    @Column(name = "user_description")
    private String description;

    @Column(name = "user_level", nullable = false)
    private Integer level = 1;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_title_id", nullable = false)
    private UserTitle title;

    @Column(name = "profile_img_url", length = 2048)
    private String profileImgUrl;

    protected User() { }

    public User(String friendCode, String email, String password, String name, UserTitle title) {
        this.friendCode = friendCode;
        this.email = email;
        this.password = password;
        this.name = name;
        this.title = title;
    }

    /** 레벨/칭호 갱신 (도감 개수가 변할 때) */
    public void updateLevel(int level, UserTitle title) {
        this.level = level;
        if (title != null) this.title = title;
    }

    public Integer getId() { return id; }

    public String getFriendCode() { return friendCode; }

    public String getEmail() { return email; }

    public String getName() { return name; }

    public String getDescription() { return description; }

    public Integer getLevel() { return level; }

    public UserTitle getTitle() { return title; }

    public String getProfileImgUrl() { return profileImgUrl; }
}
