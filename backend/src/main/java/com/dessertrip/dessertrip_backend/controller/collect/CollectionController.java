package com.dessertrip.dessertrip_backend.controller.collect;

import com.dessertrip.dessertrip_backend.dto.collect.CollectionResponse;
import com.dessertrip.dessertrip_backend.dto.collect.CollectionStatsResponse;
import com.dessertrip.dessertrip_backend.dto.collect.ProfileResponse;
import com.dessertrip.dessertrip_backend.service.collect.CollectionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 내 도감 조회 API — 경로는 팀 API 문서(/api/v1/users/me/...) 기준 */
@RestController
@RequestMapping("/api/v1/users/me")
public class CollectionController {

    private final CollectionService collectionService;

    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    /** 내 도감 목록 조회: category_id, sort(recent|oldest|name), limit, keyword */
    @GetMapping("/collection")
    public CollectionResponse list(@RequestParam(name = "category_id", required = false) Integer categoryId,
                                   @RequestParam(required = false) String sort,
                                   @RequestParam(required = false) Integer limit,
                                   @RequestParam(required = false) String keyword) {
        return collectionService.list(categoryId, sort, limit, keyword);
    }

    /** 내 도감 통계 조회 */
    @GetMapping("/collection/stats")
    public CollectionStatsResponse stats() {
        return collectionService.stats();
    }

    /** 내 프로필 정보 조회 */
    @GetMapping("/profile")
    public ProfileResponse profile() {
        return collectionService.profile();
    }
}
