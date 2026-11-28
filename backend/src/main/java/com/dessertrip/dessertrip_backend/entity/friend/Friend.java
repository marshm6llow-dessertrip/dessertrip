package com.dessertrip.dessertrip_backend.entity.friend;

import com.dessertrip.dessertrip_backend.entity.collect.BaseTimeEntity;
import com.dessertrip.dessertrip_backend.entity.collect.User;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "friend")
@SQLDelete(sql = "UPDATE friend SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Friend extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "friend_user_id", nullable = false)
    private User friendUser;

    protected Friend() {
    }

    public Friend(User user, User friendUser) {
        this.user = user;
        this.friendUser = friendUser;
    }

    public Integer getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public User getFriendUser() {
        return friendUser;
    }
}