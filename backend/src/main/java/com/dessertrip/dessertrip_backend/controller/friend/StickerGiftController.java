package com.dessertrip.dessertrip_backend.controller.friend;

import com.dessertrip.dessertrip_backend.dto.friend.*;
import com.dessertrip.dessertrip_backend.service.friend.StickerGiftService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/me/sticker-gifts")
public class StickerGiftController {

    private final StickerGiftService stickerGiftService;

    public StickerGiftController(
            StickerGiftService stickerGiftService
    ) {
        this.stickerGiftService = stickerGiftService;
    }

    // 스티커 선물 보내기
    @PostMapping
    public ResponseEntity<StickerGiftCreateResponse> sendGift(
            @Valid @RequestBody StickerGiftCreateRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(stickerGiftService.sendGift(request));
    }

    // 스티커 선물 수락 또는 거절
    @PatchMapping("/{giftId}")
    public ResponseEntity<StickerGiftUpdateResponse> updateGift(
            @PathVariable("giftId") Integer giftId,
            @Valid @RequestBody StickerGiftUpdateRequest request
    ) {
        return ResponseEntity.ok(
                stickerGiftService.updateGift(giftId, request)
        );
    }
}
