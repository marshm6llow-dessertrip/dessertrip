package com.dessertrip.dessertrip_backend.entity.friend;

import com.dessertrip.dessertrip_backend.entity.collect.BaseTimeEntity;
import com.dessertrip.dessertrip_backend.entity.collect.Sticker;
import com.dessertrip.dessertrip_backend.entity.collect.User;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "sticker_request")
@SQLDelete(sql = "UPDATE sticker_request SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class StickerRequest extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_receiver_id", nullable = false)
    private User requestReceiver;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sticker_id", nullable = false)
    private Sticker sticker;

    @Column(name = "sticker_request_message", length = 2048)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_status", nullable = false, length = 255)
    private StickerRequestStatus status = StickerRequestStatus.PENDING;

    protected StickerRequest() {
    }

    public StickerRequest(
            User requester,
            User requestReceiver,
            Sticker sticker
    ) {
        this.requester = requester;
        this.requestReceiver = requestReceiver;
        this.sticker = sticker;
        this.message = null;
        this.status = StickerRequestStatus.PENDING;
    }

    public Integer getId() {
        return id;
    }

    public User getRequester() {
        return requester;
    }

    public User getRequestReceiver() {
        return requestReceiver;
    }

    public Sticker getSticker() {
        return sticker;
    }

    public String getMessage() {
        return message;
    }

    public StickerRequestStatus getStatus() {
        return status;
    }

    public void accept() {
        this.status = StickerRequestStatus.ACCEPTED;
    }

    public void reject() {
        this.status = StickerRequestStatus.REJECTED;
    }
}