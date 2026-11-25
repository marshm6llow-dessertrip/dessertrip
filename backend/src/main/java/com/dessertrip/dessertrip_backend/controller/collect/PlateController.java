package com.dessertrip.dessertrip_backend.controller.collect;

import com.dessertrip.dessertrip_backend.dto.collect.PlateResponse;
import com.dessertrip.dessertrip_backend.dto.collect.SavePlateRequest;
import com.dessertrip.dessertrip_backend.service.collect.PlateService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 플레이트 API — 경로와 본문은 팀 API 문서(/api/v1/users/me/plate) 기준 */
@RestController
@RequestMapping("/api/v1/users/me/plate")
public class PlateController {

    private final PlateService plateService;

    public PlateController(PlateService plateService) {
        this.plateService = plateService;
    }

    /** 내 플레이트 조회 */
    @GetMapping
    public PlateResponse getPlate() {
        return plateService.getPlate();
    }

    /** 내 플레이트 스티커 저장 (sticker_ids 로 교체, 위치는 서버가 자동 배치) */
    @PutMapping("/stickers")
    public PlateResponse savePlacements(@Valid @RequestBody SavePlateRequest request) {
        return plateService.savePlacements(request);
    }
}
