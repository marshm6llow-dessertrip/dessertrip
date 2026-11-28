package com.dessertrip.dessertrip_backend.repository.collect;

import com.dessertrip.dessertrip_backend.entity.collect.Dessert;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DessertRepository extends JpaRepository<Dessert, Integer> {

    /** 내 도감 목록: 스티커/카테고리/카페는 한 번에, 사진·태그는 묶음(batch)으로 가져온다 (N+1 방지). 등록 순. */
    @EntityGraph(attributePaths = {"sticker", "category"})
    Optional<Dessert> findBySticker_IdAndUserId(
            Integer stickerId,
            Integer userId
    );
    @EntityGraph(attributePaths = {"sticker", "category", "cafe"})
    List<Dessert> findByUserIdOrderByIdAsc(Integer userId);

    @EntityGraph(attributePaths = {"sticker", "category", "cafe"})
    Optional<Dessert> findByIdAndUserId(Integer id, Integer userId);

    long countByUserId(Integer userId);

    /** 내가 직접 만든 스티커만 센다 (친구가 준 선물 스티커는 도감에 넣지 않는다) */
    long countByUserIdAndStickerPresentFalse(Integer userId);

    long countByUserIdAndCategoryIdAndStickerPresentFalse(Integer userId, Integer categoryId);

    /** 테이스팅 노트(별점)를 쓴 개수 */
    long countByUserIdAndRatingIsNotNull(Integer userId);

    long countByUserIdAndCategoryId(Integer userId, Integer categoryId);

    /** 도감 번호: 내 도감에서 이 디저트까지의 순서 (1부터) */
    long countByUserIdAndIdLessThanEqual(Integer userId, Integer id);
}
