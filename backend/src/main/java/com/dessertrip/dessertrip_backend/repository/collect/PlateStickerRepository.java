package com.dessertrip.dessertrip_backend.repository.collect;

import com.dessertrip.dessertrip_backend.entity.collect.PlateSticker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlateStickerRepository extends JpaRepository<PlateSticker, Integer> {

    List<PlateSticker> findByPlateIdOrderByDisplayOrderAsc(Integer plateId);

    /** 접시 위 스티커를 통째로 비운다 (실제 삭제) */
    @Modifying(flushAutomatically = true)
    @Query("delete from PlateSticker p where p.plateId = :plateId")
    void deleteByPlateId(@Param("plateId") Integer plateId);

    /** 스티커가 삭제될 때 접시에서도 뺀다 */
    @Modifying(flushAutomatically = true)
    @Query("delete from PlateSticker p where p.stickerId = :stickerId")
    void deleteByStickerId(@Param("stickerId") Integer stickerId);
}
