package com.example.dessertrip.ui.collect.data;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;

import com.example.dessertrip.ui.collect.core.network.ApiCallback;
import com.example.dessertrip.ui.collect.core.network.ApiError;
import com.example.dessertrip.ui.collect.core.network.ApiErrorParser;
import com.example.dessertrip.ui.collect.core.network.ImageUploader;
import com.example.dessertrip.ui.collect.core.network.UploadException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;

public class DessertRepository {
    private final DessertApi api;
    private final ApiErrorParser parser;
    private final ImageUploader uploader;
    private final Gson gson;
    private final ExecutorService io = Executors.newFixedThreadPool(2);
    private final Handler main = new Handler(Looper.getMainLooper());

    public DessertRepository(DessertApi api, ApiErrorParser parser, ImageUploader uploader, Gson gson) {
        this.api = api;
        this.parser = parser;
        this.uploader = uploader;
        this.gson = gson;
    }

    // ---------- 서버 응답(API 문서 형식) → 화면용 모델 변환 ----------

    private interface Mapper<A, B> { B map(A from); }

    /** B 를 받는 콜백을, A 를 받는 콜백으로 바꿔 준다 */
    private static <A, B> ApiCallback<A> mapped(final ApiCallback<B> cb, final Mapper<A, B> mapper) {
        return new ApiCallback<A>() {
            @Override public void onSuccess(A data) { cb.onSuccess(data == null ? null : mapper.map(data)); }
            @Override public void onError(ApiError error) { cb.onError(error); }
        };
    }

    private static ArchiveItemDto toItem(NoteDetailDto n) {
        ArchiveItemDto i = new ArchiveItemDto();
        i.id = n.noteId;
        i.number = n.collectionNumber;
        if (n.dessert != null) {
            i.name = n.dessert.dessertName == null ? "" : n.dessert.dessertName;
            i.stickerUrl = n.dessert.dessertImageUrl;
            i.category = n.dessert.category;
        }
        i.placeId = n.cafeId == null ? null : String.valueOf(n.cafeId);
        i.placeName = n.cafeName;
        i.rating = n.rating == null ? null : n.rating.floatValue();
        i.note = n.content;
        if (n.tags != null) i.tags = new ArrayList<>(n.tags);
        if (n.images != null) i.photoUrls = new ArrayList<>(n.images);
        i.tastedDate = n.visitDate;
        i.shareToCommunity = n.shareToCommunity;
        return i;
    }

    private static ArchiveItemDto toItem(CollectionDto.Item c) {
        ArchiveItemDto i = new ArchiveItemDto();
        i.id = c.dessertId;                       // 노트 id = dessert id
        i.stickerId = c.stickerId;
        i.gift = "GIFT".equals(c.acquisitionType);
        i.number = c.collectionNumber;
        i.name = c.dessertName == null ? "" : c.dessertName;
        i.stickerUrl = c.stickerImgUrl;
        i.category = c.dessertCategoryName;
        i.placeName = c.cafeName;
        i.hasTastingNote = c.hasTastingNote;
        return i;
    }

    // ---------- 도감 ----------

    public Call<ProfileDto> getProfile(ApiCallback<ProfileDto> callback) {
        return parser.enqueue(api.getProfile(), callback);
    }
    /** 도감 화면 위쪽 카드: 프로필(이름·친구 코드) + 도감 통계(레벨·진행도·카테고리별 개수) */
    public void getPlate(final ApiCallback<PlateDto> cb) {
        parser.enqueue(api.getProfile(), new ApiCallback<ProfileDto>() {
            @Override public void onSuccess(final ProfileDto profile) {
                parser.enqueue(api.getCollectionStats(), new ApiCallback<StatsDto>() {
                    @Override public void onSuccess(StatsDto stats) {
                        PlateDto p = new PlateDto();
                        if (profile != null) { p.nickname = profile.userName; p.friendCode = profile.userFriendCode; p.bio = profile.userDescription; }
                        if (stats != null) {
                            p.level = stats.userLevel;
                            p.levelTitle = stats.userTitle;
                            p.collectedCount = stats.totalStickerCount;
                            p.goalCount = Math.max(1, Math.max(stats.nextLevelRequiredCount, stats.totalStickerCount));
                            p.categoryCounts = new LinkedHashMap<>();
                            for (StatsDto.CategoryCount c : stats.categoryCounts) p.categoryCounts.put(c.categoryName, c.count);
                        }
                        cb.onSuccess(p);
                    }
                    @Override public void onError(ApiError error) { cb.onError(error); }
                });
            }
            @Override public void onError(ApiError error) { cb.onError(error); }
        });
    }

    public void getArchive(ApiCallback<List<ArchiveItemDto>> cb) {
        parser.enqueue(api.getCollection(null, "recent", null, null), mapped(cb, new Mapper<CollectionDto, List<ArchiveItemDto>>() {
            @Override public List<ArchiveItemDto> map(CollectionDto from) {
                List<ArchiveItemDto> out = new ArrayList<>();
                if (from.stickers != null) for (CollectionDto.Item c : from.stickers) out.add(toItem(c));
                return out;
            }
        }));
    }

    public void getArchiveItem(long id, ApiCallback<ArchiveItemDto> cb) {
        parser.enqueue(api.getNote(id), mapped(cb, new Mapper<NoteDetailDto, ArchiveItemDto>() {
            @Override public ArchiveItemDto map(NoteDetailDto from) { return toItem(from); }
        }));
    }

    /** 테이스팅 노트만 삭제(스티커는 남음). 끝나면 최신 상세를 돌려준다. */
    public void deleteNote(final long id, final ApiCallback<ArchiveItemDto> cb) {
        parser.enqueue(api.deleteNote(id), new ApiCallback<MessageDto>() {
            @Override public void onSuccess(MessageDto ignored) { getArchiveItem(id, cb); }
            @Override public void onError(ApiError error) { cb.onError(error); }
        });
    }

    public void deleteArchiveItem(long id, ApiCallback<Void> cb) { parser.enqueue(api.deleteArchiveItem(id), cb); }

    /** 지금 접시 위에 놓인 스티커 (sticker_id, 이미지, 위치) */
    public void getPlateStickers(ApiCallback<PlateResponseDto> cb) { parser.enqueue(api.getPlate(), cb); }

    /** 플레이트에 올릴 스티커 저장 (sticker_ids 만 보내고, 위치는 서버가 자동 배치) */
    public void savePlate(List<Long> stickerIds, ApiCallback<PlateResponseDto> cb) {
        parser.enqueue(api.savePlateStickers(new SavePlateRequest(stickerIds)), cb);
    }

    /** 검색 호출은 취소할 수 있도록 Call 을 돌려준다. */
    public Call<?> searchPlaces(String query, ApiCallback<List<PlaceDto>> cb) {
        return parser.enqueue(api.getCafes(query), mapped(cb, new Mapper<CafeListDto, List<PlaceDto>>() {
            @Override public List<PlaceDto> map(CafeListDto from) {
                List<PlaceDto> out = new ArrayList<>();
                if (from.cafes != null) for (CafeListDto.Item c : from.cafes) {
                    PlaceDto p = new PlaceDto(String.valueOf(c.cafeId), c.cafeName);
                    p.address = c.address == null ? "" : c.address;
                    p.latitude = c.latitude;
                    p.longitude = c.longitude;
                    out.add(p);
                }
                return out;
            }
        }));
    }
    public Call<List<String>> getTags(String query, ApiCallback<List<String>> cb) {
        return parser.enqueue(api.getTags(query), cb);
    }

    public Call<List<String>> getCategories(String query, ApiCallback<List<String>> cb) {
        return parser.enqueue(api.getCategories(query), cb);
    }
    public void createCategory(String name, ApiCallback<NameDto> cb) { parser.enqueue(api.createCategory(new NameDto(name)), cb); }
    public void createTag(String name, ApiCallback<NameDto> cb) { parser.enqueue(api.createTag(new NameDto(name)), cb); }
    public void updateBasics(long id, BasicsRequest req, ApiCallback<ArchiveItemDto> cb) {
        parser.enqueue(api.updateBasics(id, req), mapped(cb, new Mapper<NoteDetailDto, ArchiveItemDto>() {
            @Override public ArchiveItemDto map(NoteDetailDto from) { return toItem(from); }
        }));
    }

    /** 사진 → 배경 제거 스티커 생성 (multipart/form-data) */
    public void createSticker(final Uri uri, final ApiCallback<ArchiveItemDto> cb) {
        final ApiCallback<StickerCreatedDto> created = mapped(cb, new Mapper<StickerCreatedDto, ArchiveItemDto>() {
            @Override public ArchiveItemDto map(StickerCreatedDto from) {
                ArchiveItemDto i = new ArchiveItemDto();
                i.id = from.dessertId;               // 이후 이름·노트 저장은 dessert id(= note id)로 한다
                i.stickerUrl = from.stickerImgUrl;
                return i;
            }
        });
        io.execute(new Runnable() {
            @Override public void run() {
                try {
                    final MultipartBody.Part part = uploader.toPart("image", uri);
                    main.post(new Runnable() {
                        @Override public void run() { parser.enqueue(api.createSticker(part), created); }
                    });
                } catch (final UploadException e) {
                    postError(cb, e.error);
                }
            }
        });
    }

    /** 테이스팅 노트 저장: request(JSON) + photos(이미지 여러 장) */
    public void saveTasting(final long id, final TastingRequest request, final List<Uri> newPhotos,
                            final ApiCallback<ArchiveItemDto> cb) {
        io.execute(new Runnable() {
            @Override public void run() {
                try {
                    final List<MultipartBody.Part> parts = new ArrayList<>();
                    for (Uri uri : newPhotos) parts.add(uploader.toPart("photos", uri));
                    final RequestBody body = RequestBody.create(gson.toJson(request), MediaType.parse("application/json"));
                    main.post(new Runnable() {
                        @Override public void run() {
                            // 수정 응답에는 이름·카테고리·장소가 없으므로, 저장이 끝나면 상세를 다시 받아 화면 모델을 만든다
                            parser.enqueue(api.saveNote(id, body, parts), new ApiCallback<NoteUpdateDto>() {
                                @Override public void onSuccess(NoteUpdateDto ignored) { getArchiveItem(id, cb); }
                                @Override public void onError(ApiError error) { cb.onError(error); }
                            });
                        }
                    });
                } catch (final UploadException e) {
                    postError(cb, e.error);
                }
            }
        });
    }

    private <T> void postError(final ApiCallback<T> cb, final ApiError error) {
        main.post(new Runnable() { @Override public void run() { cb.onError(error); } });
    }
}
