package com.dessertrip.dessertrip_backend.service.collect;

import com.dessertrip.dessertrip_backend.common.ApiException;
import com.dessertrip.dessertrip_backend.common.ErrorCode;
import com.dessertrip.dessertrip_backend.common.collect.FileUrlResolver;
import com.dessertrip.dessertrip_backend.common.collect.UploadPolicy;
import com.dessertrip.dessertrip_backend.dto.collect.BasicsRequest;
import com.dessertrip.dessertrip_backend.dto.collect.NoteDetailResponse;
import com.dessertrip.dessertrip_backend.dto.collect.NoteUpdateResponse;
import com.dessertrip.dessertrip_backend.dto.collect.StickerCreatedResponse;
import com.dessertrip.dessertrip_backend.dto.collect.TastingNoteRequest;
import com.dessertrip.dessertrip_backend.entity.collect.Cafe;
import com.dessertrip.dessertrip_backend.entity.collect.Dessert;
import com.dessertrip.dessertrip_backend.entity.collect.DessertCategory;
import com.dessertrip.dessertrip_backend.entity.collect.Sticker;
import com.dessertrip.dessertrip_backend.entity.collect.Tag;
import com.dessertrip.dessertrip_backend.entity.collect.User;
import com.dessertrip.dessertrip_backend.repository.collect.CafeRepository;
import com.dessertrip.dessertrip_backend.repository.collect.CategoryRepository;
import com.dessertrip.dessertrip_backend.repository.collect.DessertRepository;
import com.dessertrip.dessertrip_backend.repository.collect.PlateStickerRepository;
import com.dessertrip.dessertrip_backend.repository.collect.StickerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 스티커 생성 + 테이스팅 노트 업무 로직. 모든 조회/수정은 "현재 사용자"의 데이터로 제한된다.
 * 스티커를 만들면 sticker 와 dessert 가 함께 만들어지고, 노트는 dessert 행에 저장된다 (note_id = dessert id).
 * 공유를 켜면 노트 내용을 복사한 post(스냅샷)가 만들어지고, 이후 노트를 고치거나 지워도 post 는 그대로다.
 * 응답 DTO 는 반드시 트랜잭션 안에서 만든다 (spring.jpa.open-in-view=false 라서 밖에서는 지연 로딩이 안 된다).
 */
@Service
public class DessertService {

    private final DessertRepository dessertRepository;
    private final StickerRepository stickerRepository;
    private final CategoryRepository categoryRepository;
    private final CafeRepository cafeRepository;
    private final PlateStickerRepository plateStickerRepository;
    private final PlateService plateService;
    private final TagService tagService;
    private final FileStorageService fileStorage;
    private final FileUrlResolver urls;
    private final CurrentUserService currentUser;
    private final UserLevelService levels;
    private final CommunityShareService community;

    public DessertService(DessertRepository dessertRepository, StickerRepository stickerRepository,
                          CategoryRepository categoryRepository, CafeRepository cafeRepository,
                          PlateStickerRepository plateStickerRepository, PlateService plateService, TagService tagService,
                          FileStorageService fileStorage, FileUrlResolver urls, CurrentUserService currentUser,
                          UserLevelService levels, CommunityShareService community) {
        this.dessertRepository = dessertRepository;
        this.stickerRepository = stickerRepository;
        this.categoryRepository = categoryRepository;
        this.cafeRepository = cafeRepository;
        this.plateStickerRepository = plateStickerRepository;
        this.plateService = plateService;
        this.tagService = tagService;
        this.fileStorage = fileStorage;
        this.urls = urls;
        this.currentUser = currentUser;
        this.levels = levels;
        this.community = community;
    }

    @Transactional(readOnly = true)
    public NoteDetailResponse get(long id) {
        return toDetail(find(currentUser.get(), id));
    }

    /**
     * 사진으로 스티커 만들기: sticker 와 dessert 를 함께 만든다. 이름·카테고리는 바로 뒤에 PATCH 로 저장된다.
     * 배경 제거는 앱(ML Kit)에서 끝낸 투명 PNG 를 받아 그대로 저장한다.
     */
    @Transactional
    public StickerCreatedResponse createSticker(MultipartFile image) {
        User me = currentUser.get();
        String fileName = fileStorage.store(image, "image");
        try {
            Sticker sticker = stickerRepository.save(new Sticker(me.getId(), Dessert.UNNAMED, fileName));
            Dessert dessert = dessertRepository.save(new Dessert(me.getId(), sticker, Dessert.UNNAMED));
            plateService.addNewSticker(me, sticker.getId());   // 새 스티커는 접시에 자동으로 올라간다
            levels.sync(me);
            return new StickerCreatedResponse(sticker.getId(), dessert.getId(), urls.toUrl(fileName),
                    sticker.getCreatedAt() == null ? LocalDateTime.now() : sticker.getCreatedAt());
        } catch (RuntimeException e) {
            fileStorage.deleteQuietly(fileName);
            throw e;
        }
    }

    /** (API 문서에 없음) 스티커 완성 직후 필수 정보(이름, 카테고리) 저장 */
    @Transactional
    public NoteDetailResponse updateBasics(long id, BasicsRequest req) {
        Dessert dessert = find(currentUser.get(), id);
        dessert.updateBasics(req.dessertName().trim(), requireCategory(req.category()));
        return toDetail(dessert);
    }

    /** 테이스팅 노트 수정(PATCH): 요청에 들어 있는 필드만 바꾼다. */
    @Transactional
    public NoteUpdateResponse updateNote(long id, TastingNoteRequest req, List<MultipartFile> newPhotos) {
        User me = currentUser.get();
        Dessert dessert = find(me, id);

        String name = dessert.getName();
        if (req.dessertName() != null) {
            name = req.dessertName().trim();
            if (name.isEmpty()) throw ApiException.ofField(ErrorCode.VALIDATION_FAILED, "dessert_name", "필수 입력값입니다.");
        }
        DessertCategory category = req.category() != null ? requireCategory(req.category()) : dessert.getCategory();

        Double rating = dessert.getRating();
        if (req.rating() != null) {
            if (req.rating() * 2 != Math.floor(req.rating() * 2)) {
                throw ApiException.ofField(ErrorCode.VALIDATION_FAILED, "rating", "0.5 단위여야 합니다.");
            }
            rating = req.rating();
        }

        Cafe cafe = dessert.getCafe();
        if (req.cafeId() != null) {
            // cafe_id 가 0 이하이면 "장소 없음"으로 되돌린다
            cafe = req.cafeId() <= 0 ? null : cafeRepository.findById(req.cafeId())
                    .orElseThrow(() -> ApiException.ofField(ErrorCode.VALIDATION_FAILED, "cafe_id", "존재하지 않는 장소입니다."));
        }

        // 유지할 기존 사진: 이 디저트가 실제로 가진 파일만 인정한다 (남의 파일 이름을 끼워 넣지 못하게). keep_images 가 없으면 전부 유지.
        List<String> current = dessert.imageFileNames();
        List<String> keep = new ArrayList<>();
        if (req.keepImages() == null) {
            keep.addAll(current);
        } else {
            Set<String> owned = new LinkedHashSet<>(current);
            for (String url : req.keepImages()) {
                String fileName = urls.toFileName(url);
                if (fileName != null && owned.contains(fileName) && !keep.contains(fileName)) keep.add(fileName);
            }
        }
        List<MultipartFile> photos = newPhotos == null ? List.of() : newPhotos.stream().filter(f -> !f.isEmpty()).toList();
        if (keep.size() + photos.size() > UploadPolicy.MAX_IMAGE_COUNT) {
            throw ApiException.ofField(ErrorCode.TOO_MANY_PHOTOS, "images",
                    "사진은 최대 " + UploadPolicy.MAX_IMAGE_COUNT + "장까지 등록할 수 있습니다.");
        }

        // 태그: 검증 후 없는 태그는 태그 목록에도 저장
        List<Tag> tags = null;
        if (req.tags() != null) {
            tags = new ArrayList<>();
            for (String raw : req.tags()) {
                Tag tag = tagService.findOrCreate(raw, "tags");
                if (!tags.contains(tag)) tags.add(tag);
            }
        }

        // 새 사진 저장 (중간에 실패하면 이미 저장한 새 파일은 지운다)
        List<String> saved = new ArrayList<>();
        try {
            for (MultipartFile photo : photos) saved.add(fileStorage.store(photo, "photos"));
        } catch (RuntimeException e) {
            saved.forEach(fileStorage::deleteQuietly);
            throw e;
        }

        List<String> removed = current.stream().filter(n -> !keep.contains(n)).toList();
        List<String> all = new ArrayList<>(keep);
        all.addAll(saved);
        dessert.updateTastingNote(name, category, cafe, rating,
                req.visitDate() != null ? req.visitDate() : dessert.getVisitDate(),
                req.content() != null ? req.content() : dessert.getDescription());
        dessert.replaceImages(all);
        if (tags != null) dessert.replaceTags(tags);
        dessertRepository.flush();
        // 공유를 켜면 (아직 게시글이 없을 때만) 지금 노트 내용을 복사한 게시글을 만든다. 이미 올린 게시글은 수정·삭제하지 않는다.
        if (Boolean.TRUE.equals(req.shareToCommunity())) community.shareIfAbsent(dessert, me.getName());

        NoteUpdateResponse response = new NoteUpdateResponse(dessert.getId(), dessert.getId(), dessert.getRating(),
                dessert.tags().stream().map(Tag::getName).toList(), dessert.getDescription(),
                dessert.imageFileNames().stream().map(urls::toUrl).toList(),
                dessert.getUpdatedAt() == null ? LocalDateTime.now() : dessert.getUpdatedAt());
        removed.stream().filter(n -> !community.usesImage(n)).forEach(fileStorage::deleteQuietly);   // 게시글이 쓰는 사진은 남긴다
        return response;
    }

    /**
     * 테이스팅 노트 삭제: 별점·후기·태그·사진·방문일·장소만 지우고, 스티커와 이름·카테고리는 남긴다.
     * 이미 커뮤니티에 올린 게시글(스냅샷)은 지우지 않으며, 게시글이 쓰는 사진 파일도 남긴다.
     */
    @Transactional
    public void deleteNote(long id) {
        Dessert dessert = find(currentUser.get(), id);
        List<String> files = new ArrayList<>(dessert.imageFileNames());
        dessert.updateTastingNote(dessert.getName(), dessert.getCategory(), null, null, null, null);
        dessert.replaceImages(List.of());
        dessert.replaceTags(List.of());
        dessertRepository.flush();
        files.stream().filter(f -> !community.usesImage(f)).forEach(fileStorage::deleteQuietly);
    }

    /** (API 문서에 없음) 스티커 삭제: 스티커와 테이스팅 노트(도감 항목)를 모두 삭제 */
    @Transactional
    public void delete(long id) {
        User me = currentUser.get();
        Dessert dessert = find(me, id);
        Sticker sticker = dessert.getSticker();
        List<String> files = new ArrayList<>(dessert.imageFileNames());
        files.add(sticker.getImgUrl());

        plateStickerRepository.deleteByStickerId(sticker.getId()); // 접시 위 배치도 함께 삭제
        dessertRepository.delete(dessert);
        stickerRepository.delete(sticker);
        dessertRepository.flush();
        levels.sync(me);
        // 게시글(스냅샷)에 쓰인 사진·스티커 파일은 남겨 둔다 (게시글은 삭제되지 않는다)
        boolean keepSticker = community.usesSticker(sticker.getId());
        files.stream()
                .filter(f -> !community.usesImage(f) && !(keepSticker && f.equals(sticker.getImgUrl())))
                .forEach(fileStorage::deleteQuietly);
    }

    // ---------------- 내부 ----------------

    private Dessert find(User me, long id) {
        if (id < 1 || id > Integer.MAX_VALUE) throw new ApiException(ErrorCode.DESSERT_NOT_FOUND);
        return dessertRepository.findByIdAndUserId((int) id, me.getId())
                .orElseThrow(() -> new ApiException(ErrorCode.DESSERT_NOT_FOUND));
    }

    private DessertCategory requireCategory(String name) {
        return categoryRepository.findFirstByNameIgnoreCaseOrderByIdAsc(name == null ? "" : name.trim())
                .orElseThrow(() -> ApiException.ofField(ErrorCode.VALIDATION_FAILED, "category",
                        "존재하지 않는 카테고리입니다. 먼저 카테고리를 추가해 주세요."));
    }

    private NoteDetailResponse toDetail(Dessert d) {
        Cafe cafe = d.getCafe();
        String name = Dessert.UNNAMED.equals(d.getName()) ? "" : d.getName();
        long number = dessertRepository.countByUserIdAndIdLessThanEqual(d.getUserId(), d.getId());
        return new NoteDetailResponse(
                d.getId(),
                new NoteDetailResponse.Dessert(d.getId(), name, urls.toUrl(d.getSticker().getImgUrl()), null,
                        d.getCategory() == null ? null : d.getCategory().getName()),
                d.getRating(),
                d.tags().stream().map(Tag::getName).toList(),
                d.getDescription(),
                d.imageFileNames().stream().map(urls::toUrl).toList(),
                d.getCreatedAt(),
                cafe == null ? null : cafe.getId(),
                cafe == null ? null : cafe.getName(),
                d.getVisitDate(),
                community.isShared(d),
                (int) number);
    }
}
