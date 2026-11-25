package com.dessertrip.dessertrip_backend.repository.collect;

import com.dessertrip.dessertrip_backend.entity.collect.DessertCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<DessertCategory, Integer> {

    Optional<DessertCategory> findFirstByNameIgnoreCaseOrderByIdAsc(String name);

    /** 사용 중(is_active)인 카테고리를 정렬 우선도 순으로 */
    List<DessertCategory> findByActiveTrueOrderByDisplayOrderAscIdAsc();

    List<DessertCategory> findByActiveTrueAndNameContainingIgnoreCaseOrderByDisplayOrderAscIdAsc(String keyword);
}
