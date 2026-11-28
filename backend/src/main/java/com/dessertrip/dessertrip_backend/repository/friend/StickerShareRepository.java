package com.dessertrip.dessertrip_backend.repository.friend;

import com.dessertrip.dessertrip_backend.entity.friend.StickerShare;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StickerShareRepository
        extends JpaRepository<StickerShare, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT s FROM StickerShare s
        WHERE s.id = :giftId
        """)
    Optional<StickerShare> findByIdForUpdate(
            @Param("giftId") Integer giftId
    );

    boolean existsBySticker_IdAndReceiver_Id(
            Integer stickerId,
            Integer receiverId
    );
}
