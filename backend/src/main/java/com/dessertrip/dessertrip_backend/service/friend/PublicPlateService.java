package com.dessertrip.dessertrip_backend.service.friend;

import com.dessertrip.dessertrip_backend.common.ApiException;
import com.dessertrip.dessertrip_backend.common.ErrorCode;
import com.dessertrip.dessertrip_backend.common.collect.FileUrlResolver;
import com.dessertrip.dessertrip_backend.common.collect.PlatePolicy;
import com.dessertrip.dessertrip_backend.dto.friend.PublicPlateResponse;
import com.dessertrip.dessertrip_backend.entity.collect.Dessert;
import com.dessertrip.dessertrip_backend.entity.collect.DessertCategory;
import com.dessertrip.dessertrip_backend.entity.collect.DessertPlate;
import com.dessertrip.dessertrip_backend.entity.collect.PlateSticker;
import com.dessertrip.dessertrip_backend.entity.collect.Sticker;
import com.dessertrip.dessertrip_backend.entity.collect.User;
import com.dessertrip.dessertrip_backend.repository.collect.CategoryRepository;
import com.dessertrip.dessertrip_backend.repository.collect.DessertRepository;
import com.dessertrip.dessertrip_backend.repository.collect.DessertPlateRepository;
import com.dessertrip.dessertrip_backend.repository.collect.PlateStickerRepository;
import com.dessertrip.dessertrip_backend.repository.collect.StickerRepository;
import com.dessertrip.dessertrip_backend.repository.collect.UserRepository;
import com.dessertrip.dessertrip_backend.service.collect.UserLevelService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class PublicPlateService {

    private final UserRepository userRepository;
    private final DessertRepository dessertRepository;
    private final CategoryRepository categoryRepository;
    private final DessertPlateRepository plateRepository;
    private final PlateStickerRepository plateStickerRepository;
    private final StickerRepository stickerRepository;
    private final UserLevelService levels;
    private final FileUrlResolver urls;

    public PublicPlateService(
            UserRepository userRepository,
            DessertRepository dessertRepository,
            CategoryRepository categoryRepository,
            DessertPlateRepository plateRepository,
            PlateStickerRepository plateStickerRepository,
            StickerRepository stickerRepository,
            UserLevelService levels,
            FileUrlResolver urls) {

        this.userRepository = userRepository;
        this.dessertRepository = dessertRepository;
        this.categoryRepository = categoryRepository;
        this.plateRepository = plateRepository;
        this.plateStickerRepository = plateStickerRepository;
        this.stickerRepository = stickerRepository;
        this.levels = levels;
        this.urls = urls;
    }

    @Transactional(readOnly = true)
    public PublicPlateResponse getPublicPlate(
            Integer userId,
            Integer categoryId) {

        // 1. 조회 대상 사용자 확인
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(
                        ErrorCode.NOT_FOUND,
                        "해당 사용자를 찾을 수 없습니다."
                ));

        // 2. 활성 카테고리 조회
        List<DessertCategory> categories =
                categoryRepository
                        .findByActiveTrueOrderByDisplayOrderAscIdAsc();

        // 3. 카테고리 파라미터 검증
        if (categoryId != null && categories.stream()
                .noneMatch(c -> categoryId.equals(c.getId()))) {

            throw new ApiException(
                    ErrorCode.VALIDATION_FAILED,
                    "유효하지 않은 카테고리입니다."
            );
        }

        // 4. 전체 수집 개수 및 레벨 계산
        int collected = (int) dessertRepository
                .countByUserIdAndStickerPresentFalse(userId);

        int level = PlatePolicy.levelFor(collected);
        String title = levels.titleNameFor(level);

        int next = PlatePolicy.nextLevelRequired(level);
        int remaining = Math.max(0, next - collected);

        // 5. 카테고리별 수집 개수
        List<PublicPlateResponse.CategoryCount> counts =
                new ArrayList<>();

        for (DessertCategory category : categories) {
            int count = (int) dessertRepository
                    .countByUserIdAndCategoryIdAndStickerPresentFalse(
                            userId,
                            category.getId()
                    );

            counts.add(new PublicPlateResponse.CategoryCount(
                    category.getId(),
                    category.getName(),
                    count
            ));
        }

        // 6. 카테고리 필터에 해당하는 스티커 ID 확인
        Set<Integer> filteredStickerIds = new HashSet<>();

        if (categoryId != null) {
            for (Dessert dessert :
                    dessertRepository.findByUserIdOrderByIdAsc(userId)) {

                if (dessert.getCategory() != null
                        && categoryId.equals(
                        dessert.getCategory().getId())) {

                    filteredStickerIds.add(
                            dessert.getSticker().getId()
                    );
                }
            }
        }

        // 7. 대상 사용자의 플레이트 조회
        DessertPlate plate = plateRepository
                .findFirstByUserIdOrderByPlateOrderAsc(userId)
                .orElse(null);

        List<PublicPlateResponse.StickerItem> items =
                new ArrayList<>();

        // 8. 플레이트에 배치된 스티커만 조회
        if (plate != null) {
            List<PlateSticker> placements =
                    plateStickerRepository
                            .findByPlateIdOrderByDisplayOrderAsc(
                                    plate.getId()
                            );

            for (PlateSticker placement : placements) {

                Integer stickerId = placement.getStickerId();

                if (categoryId != null
                        && !filteredStickerIds.contains(stickerId)) {
                    continue;
                }

                Sticker sticker = stickerRepository
                        .findById(stickerId)
                        .orElse(null);

                // 삭제된 스티커 또는 다른 사용자 소유 스티커 제외
                if (sticker == null
                        || !userId.equals(sticker.getUserId())) {
                    continue;
                }

                items.add(new PublicPlateResponse.StickerItem(
                        sticker.getId(),
                        sticker.getName(),
                        urls.toUrl(sticker.getImgUrl()),
                        placement.getPositionX(),
                        placement.getPositionY(),
                        sticker.isPresent() ? "GIFT" : "DIRECT"
                ));
            }
        }

        // 9. 응답 생성
        return new PublicPlateResponse(
                targetUser.getId(),
                targetUser.getName(),
                targetUser.getFriendCode(),
                targetUser.getDescription() == null
                        ? "" : targetUser.getDescription(),
                targetUser.getProfileImgUrl(),
                level,
                title,
                collected,
                next,
                remaining,
                counts,
                new PublicPlateResponse.Plate(
                        plate == null ? null : plate.getId(),
                        items
                )
        );
    }
}