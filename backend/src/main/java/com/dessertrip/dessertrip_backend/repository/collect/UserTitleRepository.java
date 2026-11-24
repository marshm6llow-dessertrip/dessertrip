package com.dessertrip.dessertrip_backend.repository.collect;

import com.dessertrip.dessertrip_backend.entity.collect.UserTitle;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserTitleRepository extends JpaRepository<UserTitle, Integer> {

    /** 레벨에 맞는 칭호 (min_level <= 레벨 <= max_level, max_level 이 없으면 상한 없음). pageable 로 1개만 받는다. */
    @Query("select t from UserTitle t where t.minLevel <= :level and (t.maxLevel is null or t.maxLevel >= :level) "
            + "order by t.minLevel desc")
    List<UserTitle> findForLevel(@Param("level") int level, Pageable pageable);
}
