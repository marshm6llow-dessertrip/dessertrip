package com.dessertrip.dessertrip_backend.controller.collect;

import com.dessertrip.dessertrip_backend.dto.collect.CafeListResponse;
import com.dessertrip.dessertrip_backend.service.collect.PlaceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 카페 목록 API — API 문서: GET /api/v1/cafes */
@RestController
@RequestMapping("/api/v1/cafes")
public class PlaceController {

    private final PlaceService placeService;

    public PlaceController(PlaceService placeService) {
        this.placeService = placeService;
    }

    @GetMapping
    public CafeListResponse list(@RequestParam(required = false) String keyword,
                                 @RequestParam(required = false) String bounds,
                                 @RequestParam(name = "category_id", required = false) Integer categoryId,
                                 @RequestParam(name = "min_price", required = false) Integer minPrice,
                                 @RequestParam(name = "max_price", required = false) Integer maxPrice,
                                 @RequestParam(name = "tag_ids", required = false) String tagIds,
                                 @RequestParam(required = false) Integer zoom) {
        return placeService.search(keyword, bounds);   // category_id, 가격, 태그, zoom 은 아직 적용하지 않는다
    }
}
