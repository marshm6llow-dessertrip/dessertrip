package com.dessertrip.dessertrip_backend.repository.friend;

import com.dessertrip.dessertrip_backend.entity.friend.StickerRequest;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StickerRequestRepository
        extends JpaRepository<StickerRequest, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT r
            FROM StickerRequest r
            WHERE r.id = :requestId
            """)
    Optional<StickerRequest> findByIdForUpdate(
            @Param("requestId") Integer requestId
    );
}