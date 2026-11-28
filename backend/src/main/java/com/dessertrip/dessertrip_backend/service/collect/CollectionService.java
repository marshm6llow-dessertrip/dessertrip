package com.dessertrip.dessertrip_backend.service.collect;

import com.dessertrip.dessertrip_backend.common.ApiException;
import com.dessertrip.dessertrip_backend.common.ErrorCode;
import com.dessertrip.dessertrip_backend.common.collect.FileUrlResolver;
import com.dessertrip.dessertrip_backend.common.collect.PlatePolicy;
import com.dessertrip.dessertrip_backend.dto.collect.CollectionResponse;
import com.dessertrip.dessertrip_backend.dto.collect.CollectionStatsResponse;
import com.dessertrip.dessertrip_backend.dto.collect.ProfileResponse;
import com.dessertrip.dessertrip_backend.entity.collect.Dessert;
import com.dessertrip.dessertrip_backend.entity.collect.DessertCategory;
import com.dessertrip.dessertrip_backend.entity.collect.User;
import com.dessertrip.dessertrip_backend.repository.collect.CategoryRepository;
import com.dessertrip.dessertrip_backend.repository.collect.DessertRepository;
import com.dessertrip.dessertrip_backend.repository.friend.FriendRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 내 도감 조회: 목록(GET collection), 통계(GET collection/stats), 프로필(GET profile).
 * 응답 DTO 는 트랜잭션 안에서 만든다 (open-in-view=false).
 */
@Service
public class CollectionService {

    private static final int RECENT_LIMIT = 3;

    private final DessertRepository dessertRepository;
    private final CategoryRepository categoryRepository;
    private final CurrentUserService currentUser;
    private final UserLevelService levels;
    private final FileUrlResolver urls;
    private final FriendRepository friendRepository;

    public CollectionService(
            DessertRepository dessertRepository,
            CategoryRepository categoryRepository,
            CurrentUserService currentUser,
            UserLevelService levels,
            FileUrlResolver urls,
            FriendRepository friendRepository
    ) {
        this.dessertRepository = dessertRepository;
        this.categoryRepository = categoryRepository;
        this.currentUser = currentUser;
        this.levels = levels;
        this.urls = urls;
        this.friendRepository = friendRepository;
    }

    /** sort: recent(기본, 최근 수집순) | oldest | name */
    @Transactional(readOnly = true)
    public CollectionResponse list(Integer categoryId, String sort, Integer limit, String keyword) {
        User me = currentUser.get();
        String order = sort == null || sort.isBlank() ? "recent" : sort.trim();
        if (!List.of("recent", "oldest", "name").contains(order)) {
            throw ApiException.ofField(ErrorCode.VALIDATION_FAILED, "sort", "recent, oldest, name 중 하나여야 합니다.");
        }
        if (limit != null && limit < 1) {
            throw ApiException.ofField(ErrorCode.VALIDATION_FAILED, "limit", "1 이상이어야 합니다.");
        }
        String word = keyword == null ? "" : keyword.trim().toLowerCase();

        List<Dessert> all = dessertRepository.findByUserIdOrderByIdAsc(me.getId());
        // 도감 번호: 필터와 상관없이 내 도감 전체에서의 순서. 선물로 받은 스티커는 도감 번호를 쓰지 않으므로 따로 센다.
        Map<Integer, Integer> numbers = new HashMap<>();
        int direct = 0, gift = 0;
        for (Dessert d : all) numbers.put(d.getId(), d.getSticker().isPresent() ? ++gift : ++direct);

        List<Dessert> picked = new ArrayList<>();
        for (Dessert d : all) {
            if (categoryId != null && (d.getCategory() == null || !categoryId.equals(d.getCategory().getId()))) continue;
            if (!word.isEmpty() && !shownName(d).toLowerCase().contains(word)) continue;
            picked.add(d);
        }
        Comparator<Dessert> byCreated = Comparator.comparing((Dessert d) -> d.getSticker().getCreatedAt(),
                Comparator.nullsFirst(Comparator.<LocalDateTime>naturalOrder())).thenComparing(Dessert::getId);
        switch (order) {
            case "oldest" -> picked.sort(byCreated);
            case "name" -> picked.sort(Comparator.comparing((Dessert d) -> shownName(d)).thenComparing(Dessert::getId));
            default -> picked.sort(byCreated.reversed());
        }
        if (limit != null && picked.size() > limit) picked = new ArrayList<>(picked.subList(0, limit));

        List<CollectionResponse.Item> items = new ArrayList<>();
        for (Dessert d : picked) {
            DessertCategory c = d.getCategory();
            items.add(new CollectionResponse.Item(
                    d.getSticker().getId(), d.getId(), shownName(d),
                    c == null ? null : c.getId(),
                    urls.toUrl(d.getSticker().getImgUrl()),
                    d.getCafe() == null ? null : d.getCafe().getName(),
                    d.getSticker().isPresent() ? "GIFT" : "DIRECT",
                    d.getSticker().getCreatedAt(),
                    c == null ? null : c.getName(),
                    hasNote(d),
                    numbers.get(d.getId())));
        }
        return new CollectionResponse(items);
    }

    @Transactional(readOnly = true)
    public CollectionStatsResponse stats() {
        User me = currentUser.get();
        int collected = (int) dessertRepository.countByUserIdAndStickerPresentFalse(me.getId());   // 선물 스티커는 제외
        int level = PlatePolicy.levelFor(collected);
        int next = PlatePolicy.nextLevelRequired(level);

        List<CollectionStatsResponse.CategoryCount> counts = new ArrayList<>();
        for (DessertCategory c : categoryRepository.findByActiveTrueOrderByDisplayOrderAscIdAsc()) {
            counts.add(new CollectionStatsResponse.CategoryCount(c.getId(), c.getName(),
                    (int) dessertRepository.countByUserIdAndCategoryIdAndStickerPresentFalse(me.getId(), c.getId())));
        }
        return new CollectionStatsResponse(level, levels.titleNameFor(level), collected, next,
                Math.max(0, next - collected), PlatePolicy.progressPercent(level, collected), counts);
    }

    @Transactional(readOnly = true)
    public ProfileResponse profile() {
        User me = currentUser.get();
        int collected = (int) dessertRepository.countByUserIdAndStickerPresentFalse(me.getId());   // 선물 스티커는 제외
        int level = PlatePolicy.levelFor(collected);
        int next = PlatePolicy.nextLevelRequired(level);

        List<Dessert> recent = new ArrayList<>(dessertRepository.findByUserIdOrderByIdAsc(me.getId()));
        recent.sort(Comparator.comparing((Dessert d) -> d.getSticker().getCreatedAt(),
                Comparator.nullsFirst(Comparator.<LocalDateTime>naturalOrder())).thenComparing(Dessert::getId).reversed());
        List<ProfileResponse.RecentSticker> stickers = recent.stream().limit(RECENT_LIMIT)
                .map(d -> new ProfileResponse.RecentSticker(d.getSticker().getId(), shownName(d),
                        urls.toUrl(d.getSticker().getImgUrl())))
                .toList();

        return new ProfileResponse(me.getId(), me.getName(), me.getFriendCode(),
                me.getDescription() == null ? "" : me.getDescription(),
                level, levels.titleNameFor(level), collected,
                (int) dessertRepository.countByUserIdAndRatingIsNotNull(me.getId()),
                friendRepository.findAllByUserId(me.getId()).size(),
                Math.max(0, next - collected), stickers);
    }

    private static String shownName(Dessert d) {
        return Dessert.UNNAMED.equals(d.getName()) ? "" : d.getName();
    }

    private static boolean hasNote(Dessert d) {
        return d.getRating() != null || (d.getDescription() != null && !d.getDescription().isBlank());
    }
}
