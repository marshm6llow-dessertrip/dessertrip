package com.dessertrip.dessertrip_backend.repository.collect;

import com.dessertrip.dessertrip_backend.entity.collect.Sticker;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StickerRepository extends JpaRepository<Sticker, Integer> {

    boolean existsByIdAndUserId(Integer id, Integer userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Sticker s WHERE s.id = :stickerId")
    Optional<Sticker> findByIdForUpdate(
            @Param("stickerId") Integer stickerId
    );
}