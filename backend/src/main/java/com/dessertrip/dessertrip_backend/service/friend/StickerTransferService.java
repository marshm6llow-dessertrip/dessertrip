
package com.dessertrip.dessertrip_backend.service.friend;

import com.dessertrip.dessertrip_backend.common.ApiException;
import com.dessertrip.dessertrip_backend.common.ErrorCode;
import com.dessertrip.dessertrip_backend.entity.collect.Dessert;
import com.dessertrip.dessertrip_backend.entity.collect.Sticker;
import com.dessertrip.dessertrip_backend.entity.collect.User;
import com.dessertrip.dessertrip_backend.repository.collect.DessertRepository;
import com.dessertrip.dessertrip_backend.repository.collect.StickerRepository;
import com.dessertrip.dessertrip_backend.service.collect.PlateService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StickerTransferService {

    private final StickerRepository stickerRepository;
    private final DessertRepository dessertRepository;
    private final PlateService plateService;

    public StickerTransferService(
            StickerRepository stickerRepository,
            DessertRepository dessertRepository,
            PlateService plateService
    ) {
        this.stickerRepository = stickerRepository;
        this.dessertRepository = dessertRepository;
        this.plateService = plateService;
    }

    @Transactional
    public Integer copySticker(Sticker source, User receiver) {

        // 1. 선물받은 스티커는 다시 전달할 수 없음
        if (source.isPresent()) {
            throw new ApiException(
                    ErrorCode.VALIDATION_FAILED,
                    "선물받은 스티커는 다시 전달할 수 없습니다."
            );
        }

        // 2. 원본 디저트 조회
        Dessert originalDessert = dessertRepository
                .findBySticker_IdAndUserId(
                        source.getId(),
                        source.getUserId()
                )
                .orElseThrow(() -> new ApiException(
                        ErrorCode.DESSERT_NOT_FOUND,
                        "원본 디저트 정보를 찾을 수 없습니다."
                ));

        // 3. 수신자 소유의 새 스티커 생성
        Sticker receivedSticker = stickerRepository.save(
                Sticker.gift(receiver.getId(), source)
        );

        // 4. 수신자의 디저트 정보 생성
        Dessert receivedDessert = new Dessert(
                receiver.getId(),
                receivedSticker,
                originalDessert.getName()
        );

        receivedDessert.updateBasics(
                originalDessert.getName(),
                originalDessert.getCategory()
        );

        dessertRepository.save(receivedDessert);

        // 5. 플레이트 자동 배치
        plateService.addNewSticker(
                receiver,
                receivedSticker.getId()
        );

        // 6. 새 스티커 ID 반환
        return receivedSticker.getId();
    }
}
