package com.dessertrip.dessertrip_backend.repository.collect;

import com.dessertrip.dessertrip_backend.entity.collect.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Integer> {

    /** 같은 이름이 여러 개 있어도(이름 UNIQUE 가 없는 명세서) 가장 먼저 만든 것 하나 */
    Optional<Tag> findFirstByNameIgnoreCaseOrderByIdAsc(String name);

    /** 검색어가 없을 때: 앞에서부터 pageable 크기만큼 */
    List<Tag> findAllByOrderByIdAsc(Pageable pageable);

    List<Tag> findByNameContainingIgnoreCaseOrderByIdAsc(String keyword);
}
