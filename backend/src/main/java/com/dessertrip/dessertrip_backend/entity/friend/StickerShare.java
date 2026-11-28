
package com.dessertrip.dessertrip_backend.entity.friend;

import com.dessertrip.dessertrip_backend.entity.collect.BaseTimeEntity;
import com.dessertrip.dessertrip_backend.entity.collect.Sticker;
import com.dessertrip.dessertrip_backend.entity.collect.User;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "sticker_share")
@SQLDelete(sql = "UPDATE sticker_share SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class StickerShare extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sticker_id", nullable = false)
    private Sticker sticker;

    @Column(name = "sticker_send_message", length = 2048)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "share_status", nullable = false, length = 255)
    private StickerShareStatus status = StickerShareStatus.PENDING;

    protected StickerShare() {
    }

    public StickerShare(
            User sender,
            User receiver,
            Sticker sticker,
            String message
    ) {
        this.sender = sender;
        this.receiver = receiver;
        this.sticker = sticker;
        this.message = message;
        this.status = StickerShareStatus.PENDING;
    }

    public Integer getId() {
        return id;
    }

    public User getSender() {
        return sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public Sticker getSticker() {
        return sticker;
    }

    public String getMessage() {
        return message;
    }

    public StickerShareStatus getStatus() {
        return status;
    }

    public void accept() {
        this.status = StickerShareStatus.ACCEPTED;
    }

    public void reject() {
        this.status = StickerShareStatus.REJECTED;
    }
}
