package com.example.dessertrip.ui.archive.sticker;

import com.example.dessertrip.ui.archive.collect.NoteForm;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.dessertrip.ui.collect.core.network.ApiCallback;
import com.example.dessertrip.ui.collect.core.network.ApiError;
import com.example.dessertrip.ui.collect.core.network.UploadPolicy;
import com.example.dessertrip.ui.collect.core.util.Event;
import com.example.dessertrip.ui.collect.data.ArchiveItemDto;
import com.example.dessertrip.ui.collect.data.BasicsRequest;
import com.example.dessertrip.ui.collect.data.DessertCategories;
import com.example.dessertrip.ui.collect.data.NameDto;
import com.example.dessertrip.ui.collect.data.DessertRepository;
import com.example.dessertrip.ui.collect.data.PlaceDto;
import com.example.dessertrip.ui.collect.data.TastingRequest;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import retrofit2.Call;

/** 기록하기 플로우(등록 → 사진 → 스티커 → 노트 → 저장) 전체가 공유하는 ViewModel (nav graph 범위) */
public class RegisterFlowViewModel extends ViewModel {
    public static final List<String> DEFAULT_TAGS =
            Arrays.asList("적당히 달아요", "고소해요", "상큼해요", "촉촉해요", "바삭해요", "크림이 부드러워요");
    public static final int MAX_TAGS = 5;          // TODO 팀 협의 후 확정
    public static final int MAX_NOTE_LENGTH = 1000;
    public static final int MAX_NAME_LENGTH = 20;  // 카테고리/태그 이름 길이 (TODO 팀 협의 후 확정, 서버와 동일해야 함)
    private static final long DEBOUNCE_MS = 300;

    private final DessertRepository repo;
    private final BackgroundRemover remover;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final MutableLiveData<Uri> _pickedUri = new MutableLiveData<>();
    private final MutableLiveData<StickerStage> _stage = new MutableLiveData<>(StickerStage.PREVIEW);
    private final MutableLiveData<ArchiveItemDto> _target = new MutableLiveData<>();  // 스티커가 만들어진(또는 수정 대상) 도감 항목
    private final MutableLiveData<NoteForm> _form = new MutableLiveData<>(new NoteForm());
    private final MutableLiveData<Map<String, String>> _fieldErrors = new MutableLiveData<>(new HashMap<String, String>());
    private final MutableLiveData<Boolean> _saving = new MutableLiveData<>(false);
    private final MutableLiveData<ArchiveItemDto> _saved = new MutableLiveData<>();
    private final MutableLiveData<Event<String>> _message = new MutableLiveData<>();
    private final MutableLiveData<List<String>> _tagCandidates = new MutableLiveData<>(DEFAULT_TAGS);
    private final MutableLiveData<List<PlaceDto>> _places = new MutableLiveData<>(new ArrayList<PlaceDto>());
    private final MutableLiveData<Boolean> _placesLoading = new MutableLiveData<>(false);
    private final MutableLiveData<List<String>> _categoryCandidates = new MutableLiveData<>(new ArrayList<>(DessertCategories.DEFAULTS));
    private final MutableLiveData<Boolean> _categoriesLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> _categoryAdding = new MutableLiveData<>(false);
    private final MutableLiveData<Event<String>> _categoryPicked = new MutableLiveData<>();
    private final MutableLiveData<Event<Boolean>> _clearTagInput = new MutableLiveData<>();
    private final MutableLiveData<Event<Boolean>> _basicsDone = new MutableLiveData<>();
    private final MutableLiveData<Event<Boolean>> _discarded = new MutableLiveData<>();

    public final LiveData<Uri> pickedUri = _pickedUri;
    public final LiveData<StickerStage> stage = _stage;
    public final LiveData<ArchiveItemDto> target = _target;
    /** form 은 변경 시마다 같은 객체를 다시 set 해서 관찰자를 깨운다. */
    public final LiveData<NoteForm> form = _form;
    public final LiveData<Map<String, String>> fieldErrors = _fieldErrors;
    public final LiveData<Boolean> saving = _saving;
    /** 스티커 만들기를 취소(삭제)한 뒤 화면을 나가도 된다는 신호 */
    public final LiveData<Event<Boolean>> discarded = _discarded;
    public final LiveData<ArchiveItemDto> saved = _saved;
    public final LiveData<Event<String>> message = _message;
    public final LiveData<List<String>> tagCandidates = _tagCandidates;
    public final LiveData<List<PlaceDto>> places = _places;
    public final LiveData<Boolean> placesLoading = _placesLoading;
    public final LiveData<List<String>> categoryCandidates = _categoryCandidates;
    public final LiveData<Boolean> categoriesLoading = _categoriesLoading;
    public final LiveData<Boolean> categoryAdding = _categoryAdding;
    /** 카테고리 시트에서 항목을 고르거나 새로 추가했을 때(시트를 닫는 신호) */
    public final LiveData<Event<String>> categoryPicked = _categoryPicked;
    /** 태그를 새로 추가한 뒤 검색창을 비우라는 신호 */
    public final LiveData<Event<Boolean>> clearTagInput = _clearTagInput;
    /** 스티커 완성 화면에서 이름/카테고리 저장이 끝났을 때. 값: true=노트로 이동, false=스티커만 저장하고 종료 */
    public final LiveData<Event<Boolean>> basicsDone = _basicsDone;

    private String tagQuery = "";
    private String tagResultQuery = "";      // 마지막으로 받은 태그 검색 결과가 어떤 검색어의 것인지
    private String placeQuery = "";
    private String categoryQuery = "";
    private String categoryResultQuery = "";
    private Call<List<String>> tagCall;
    private Call<?> placeCall;
    private Call<List<String>> categoryCall;
    private Runnable tagTask, placeTask, categoryTask;
    private Future<?> removal;          // 진행 중인 배경 제거
    private int stickerRun;              // 오래된 결과를 버리기 위한 번호

    public RegisterFlowViewModel(DessertRepository repo, BackgroundRemover remover) {
        this.repo = repo;
        this.remover = remover;
        remover.warmUp();            // 모델/분할기를 미리 준비해 첫 배경 제거를 빠르게
    }

    public String getTagQuery() { return tagQuery; }
    public String getPlaceQuery() { return placeQuery; }
    public String getCategoryQuery() { return categoryQuery; }

    private NoteForm f() { return _form.getValue(); }
    private void notifyForm() { _form.setValue(_form.getValue()); }
    private void clearError(String key) {
        Map<String, String> e = new HashMap<>(_fieldErrors.getValue());
        if (e.remove(key) != null) _fieldErrors.setValue(e);
    }

    // ---------- 사진 → 스티커 ----------
    public void setPicked(Uri uri) {
        cancelRemoval();
        _pickedUri.setValue(uri);
        _stage.setValue(StickerStage.PREVIEW);
        _target.setValue(null);
        _form.setValue(new NoteForm());
        _saved.setValue(null);
    }

    /**
     * 사진 → 배경 제거(기기 안, ML Kit) → 투명 PNG 스티커를 서버에 올려 도감 항목 생성.
     * 서버는 배경을 지우지 않고 받은 스티커 이미지를 그대로 저장한다.
     */
    public void createSticker() {
        Uri uri = _pickedUri.getValue();
        if (uri == null || _stage.getValue() == StickerStage.LOADING) return;
        _stage.setValue(StickerStage.LOADING);
        final int run = ++stickerRun;
        removal = remover.remove(uri, new BackgroundRemover.Callback() {
            @Override public void onSuccess(final File png) {
                if (run != stickerRun) { deleteQuietly(png); return; }     // 그 사이 다른 사진을 골랐거나 화면을 떠남
                repo.createSticker(Uri.fromFile(png), new ApiCallback<ArchiveItemDto>() {
                    @Override public void onSuccess(ArchiveItemDto data) {
                        deleteQuietly(png);
                        if (run != stickerRun) return;
                        _target.setValue(data);
                        if (data != null && data.name != null) { f().name = data.name; notifyForm(); }
                        _stage.setValue(StickerStage.DONE);
                    }
                    @Override public void onError(ApiError error) {
                        deleteQuietly(png);
                        if (run != stickerRun) return;
                        _stage.setValue(StickerStage.PREVIEW);
                        _message.setValue(new Event<>(error.userMessage));
                    }
                });
            }
            @Override public void onError(String userMessage) {
                if (run != stickerRun) return;
                _stage.setValue(StickerStage.PREVIEW);
                _message.setValue(new Event<>(userMessage));
            }
        });
    }

    private void cancelRemoval() {
        stickerRun++;
        if (removal != null) { removal.cancel(true); removal = null; }
    }

    private static void deleteQuietly(File f) {
        //noinspection ResultOfMethodCallIgnored
        if (f != null) f.delete();
    }

    // ---------- 도감에서 기존 항목 노트 작성/수정 ----------
    public void loadExisting(long id) {
        ArchiveItemDto cur = _target.getValue();
        if (cur != null && cur.id == id) return;
        repo.getArchiveItem(id, new ApiCallback<ArchiveItemDto>() {
            @Override public void onSuccess(ArchiveItemDto i) {
                NoteForm nf = new NoteForm();
                try { if (i.tastedDate != null) nf.tastedDate = LocalDate.parse(i.tastedDate); } catch (Exception ignored) { }
                nf.rating = i.rating == null ? 0f : i.rating;
                nf.name = i.name == null ? "" : i.name;
                if (i.placeName != null) nf.place = new PlaceDto(i.placeId == null ? "" : i.placeId, i.placeName);
                nf.category = i.category;
                nf.tags = new LinkedHashSet<>(i.tagsSafe());
                nf.note = i.note == null ? "" : i.note;
                nf.existingPhotoUrls = new ArrayList<>(i.photosSafe());
                nf.share = i.shareToCommunity;
                _target.setValue(i);
                _saved.setValue(null);
                _form.setValue(nf);
            }
            @Override public void onError(ApiError error) { _message.setValue(new Event<>(error.userMessage)); }
        });
    }

    // ---------- 폼 ----------
    public void setDate(LocalDate d) { f().tastedDate = d; notifyForm(); }
    public void setRating(float r) { f().rating = r; clearError("rating"); notifyForm(); }
    public void setName(String v) { f().name = v; clearError("dessertName"); }          // 텍스트 입력은 재렌더 불필요
    public void setNote(String v) { f().note = v; clearError("note"); }
    public void setCategory(String c) { f().category = c; clearError("category"); notifyForm(); }
    public void setPlace(PlaceDto p) { f().place = p; notifyForm(); }
    public void setShare(boolean v) { f().share = v; }

    public void toggleTag(String tag) {
        NoteForm nf = f();
        if (nf.tags.contains(tag)) nf.tags.remove(tag);
        else if (nf.tags.size() >= MAX_TAGS) {
            _message.setValue(new Event<>("태그는 최대 " + MAX_TAGS + "개까지 선택할 수 있어요."));
        } else nf.tags.add(tag);
        clearError("tags");
        notifyForm();
    }

    /** 선택한 태그를 먼저, 그 뒤에 추천/검색 결과 */
    public List<String> tagChips() {
        Set<String> all = new LinkedHashSet<>(f().tags);
        all.addAll(_tagCandidates.getValue());
        return new ArrayList<>(all);
    }

    public void addPhotos(List<Uri> uris) {
        NoteForm nf = f();
        int room = Math.max(0, UploadPolicy.MAX_PHOTOS - nf.photoCount());
        for (int i = 0; i < uris.size() && i < room; i++) nf.newPhotos.add(uris.get(i));
        notifyForm();
    }
    public void removeNewPhoto(Uri uri) { f().newPhotos.remove(uri); notifyForm(); }
    public void removeExistingPhoto(String url) { f().existingPhotoUrls.remove(url); notifyForm(); }

    // ---------- 태그/장소 검색 (300ms 디바운스, 이전 호출 취소) ----------
    public void setTagQuery(String q) {
        final String query = q.trim();
        if (query.equals(tagQuery)) return;
        tagQuery = query;
        _tagCandidates.setValue(_tagCandidates.getValue());   // 검색어가 바뀌면 "추가" 칩을 일단 숨긴다
        if (tagTask != null) handler.removeCallbacks(tagTask);
        tagTask = new Runnable() {
            @Override public void run() {
                if (tagCall != null) tagCall.cancel();
                tagCall = repo.getTags(query.isEmpty() ? null : query, new ApiCallback<List<String>>() {
                    @Override public void onSuccess(List<String> data) {
                        List<String> list = data == null ? Collections.<String>emptyList() : data;
                        tagResultQuery = query;
                        _tagCandidates.setValue(list.isEmpty() && query.isEmpty() ? DEFAULT_TAGS : list);
                    }
                    @Override public void onError(ApiError error) { /* 실패 시 기존 추천 태그 유지 */ }
                });
            }
        };
        handler.postDelayed(tagTask, DEBOUNCE_MS);
    }

    public void setPlaceQuery(String q) {
        final String query = q.trim();
        if (query.equals(placeQuery)) return;
        placeQuery = query;
        if (placeTask != null) handler.removeCallbacks(placeTask);
        if (placeCall != null) placeCall.cancel();
        if (query.isEmpty()) {
            _places.setValue(new ArrayList<PlaceDto>());
            _placesLoading.setValue(false);
            return;
        }
        placeTask = new Runnable() {
            @Override public void run() {
                _placesLoading.setValue(true);
                placeCall = repo.searchPlaces(query, new ApiCallback<List<PlaceDto>>() {
                    @Override public void onSuccess(List<PlaceDto> data) {
                        _placesLoading.setValue(false);
                        _places.setValue(data == null ? new ArrayList<PlaceDto>() : data);
                    }
                    @Override public void onError(ApiError error) {
                        _placesLoading.setValue(false);
                        _message.setValue(new Event<>(error.userMessage));
                    }
                });
            }
        };
        handler.postDelayed(placeTask, DEBOUNCE_MS);
    }

    /** 태그 목록을 현재 검색어 기준으로 다시 불러온다. (화면에 들어올 때 서버에 새로 추가된 태그를 반영) */
    public void refreshTags() {
        final String query = tagQuery;
        if (tagCall != null) tagCall.cancel();
        tagCall = repo.getTags(query.isEmpty() ? null : query, new ApiCallback<List<String>>() {
            @Override public void onSuccess(List<String> data) {
                List<String> list = data == null ? Collections.<String>emptyList() : data;
                tagResultQuery = query;
                _tagCandidates.setValue(list.isEmpty() && query.isEmpty() ? DEFAULT_TAGS : list);
            }
            @Override public void onError(ApiError error) { }
        });
    }

    /** 검색어가 있고, 검색 결과(선택된 태그 포함)에 같은 이름이 없으면 새 태그로 추가할 수 있다. */
    public boolean canAddTag() {
        String q = tagQuery;
        if (q.isEmpty() || q.length() > MAX_NAME_LENGTH || !q.equals(tagResultQuery)) return false;
        for (String t : tagChips()) if (t.equalsIgnoreCase(q)) return false;
        return true;
    }

    /** 새 태그를 서버 태그 목록에 저장하고(POST /tags) 바로 선택한다. */
    public void addTag(String name) {
        final String clean = name.trim();
        if (clean.isEmpty() || clean.length() > MAX_NAME_LENGTH) return;
        repo.createTag(clean, new ApiCallback<NameDto>() {
            @Override public void onSuccess(NameDto data) {
                String saved = data != null && data.name != null ? data.name : clean;
                List<String> list = new ArrayList<>(_tagCandidates.getValue());
                if (!list.contains(saved)) list.add(0, saved);
                _tagCandidates.setValue(list);
                if (!f().tags.contains(saved)) toggleTag(saved);
                _clearTagInput.setValue(new Event<>(true));
            }
            @Override public void onError(ApiError error) { _message.setValue(new Event<>(error.userMessage)); }
        });
    }

    // ---------- 카테고리 검색/추가 ----------
    public void setCategoryQuery(String q) {
        final String query = q.trim();
        if (query.equals(categoryQuery)) return;
        categoryQuery = query;
        _categoryCandidates.setValue(_categoryCandidates.getValue());   // "추가" 행을 일단 숨긴다
        if (categoryTask != null) handler.removeCallbacks(categoryTask);
        categoryTask = new Runnable() { @Override public void run() { loadCategories(); } };
        handler.postDelayed(categoryTask, DEBOUNCE_MS);
    }

    /** 시트를 열 때: 현재 검색어 기준으로 즉시 다시 불러온다. */
    public void refreshCategories() { loadCategories(); }

    private void loadCategories() {
        final String query = categoryQuery;
        if (categoryCall != null) categoryCall.cancel();
        _categoriesLoading.setValue(true);
        categoryCall = repo.getCategories(query.isEmpty() ? null : query, new ApiCallback<List<String>>() {
            @Override public void onSuccess(List<String> data) {
                _categoriesLoading.setValue(false);
                List<String> list = data == null ? new ArrayList<String>() : data;
                categoryResultQuery = query;
                _categoryCandidates.setValue(list);
            }
            @Override public void onError(ApiError error) {
                _categoriesLoading.setValue(false);
                _message.setValue(new Event<>(error.userMessage));
            }
        });
    }

    public boolean canAddCategory() {
        String q = categoryQuery;
        if (q.isEmpty() || q.length() > MAX_NAME_LENGTH || !q.equals(categoryResultQuery)) return false;
        for (String c : _categoryCandidates.getValue()) if (c.equalsIgnoreCase(q)) return false;
        return true;
    }

    public void pickCategory(String name) {
        setCategory(name);
        _categoryPicked.setValue(new Event<>(name));
    }

    /** 새 카테고리를 서버 카테고리 목록에 저장하고(POST /categories) 바로 선택한다. */
    public void addCategory(String name) {
        final String clean = name.trim();
        if (clean.isEmpty() || clean.length() > MAX_NAME_LENGTH || Boolean.TRUE.equals(_categoryAdding.getValue())) return;
        _categoryAdding.setValue(true);
        repo.createCategory(clean, new ApiCallback<NameDto>() {
            @Override public void onSuccess(NameDto data) {
                _categoryAdding.setValue(false);
                pickCategory(data != null && data.name != null ? data.name : clean);
            }
            @Override public void onError(ApiError error) {
                _categoryAdding.setValue(false);
                _message.setValue(new Event<>(error.userMessage));
            }
        });
    }

    // ---------- 스티커 완성 화면: 이름·카테고리 필수 저장 ----------
    /**
     * "스티커 완성!" 화면에서 취소: 방금 서버에 만들어진 스티커(와 도감 항목)를 지우고 나간다.
     * 삭제가 실패해도 화면은 나가되, 안내 메시지를 보여 준다.
     */
    public void discardCreated() {
        if (Boolean.TRUE.equals(_saving.getValue())) return;
        ArchiveItemDto t = _target.getValue();
        if (t == null) { _discarded.setValue(new Event<>(true)); return; }
        _saving.setValue(true);
        repo.deleteArchiveItem(t.id, new ApiCallback<Void>() {
            @Override public void onSuccess(Void data) {
                _saving.setValue(false);
                _discarded.setValue(new Event<>(true));
            }
            @Override public void onError(ApiError error) {
                _saving.setValue(false);
                _message.setValue(new Event<>("스티커 삭제에 실패했어요. 도감에서 직접 삭제해 주세요."));
                _discarded.setValue(new Event<>(true));
            }
        });
    }

    public void requireBasicsMessage() { _message.setValue(new Event<>("이름과 카테고리를 입력한 뒤 저장해 주세요.")); }

    /** 이름/카테고리를 검증하고 서버에 저장한다. 성공하면 basicsDone 이벤트(thenNote 전달). */
    public void submitBasics(final boolean thenNote) {
        ArchiveItemDto t = _target.getValue();
        if (t == null || Boolean.TRUE.equals(_saving.getValue())) return;
        NoteForm f = f();
        boolean noName = f.name == null || f.name.trim().isEmpty();
        boolean noCategory = f.category == null || f.category.trim().isEmpty();
        // 이름과 카테고리는 필수: 둘 다 채워야 저장하고 다음으로 넘어간다.
        Map<String, String> errors = new HashMap<>();
        if (noName) errors.put("dessertName", "디저트 이름을 입력해 주세요.");
        if (noCategory) errors.put("category", "카테고리를 선택해 주세요.");
        if (!errors.isEmpty()) {
            _fieldErrors.setValue(errors);
            _message.setValue(new Event<>("이름과 카테고리를 입력해 주세요."));
            return;
        }
        _fieldErrors.setValue(new HashMap<String, String>());
        _saving.setValue(true);
        repo.updateBasics(t.id, new BasicsRequest(f.name.trim(), f.category), new ApiCallback<ArchiveItemDto>() {
            @Override public void onSuccess(ArchiveItemDto data) {
                _saving.setValue(false);
                if (data != null) _target.setValue(data);
                _basicsDone.setValue(new Event<>(thenNote));
            }
            @Override public void onError(ApiError error) {
                _saving.setValue(false);
                _fieldErrors.setValue(uiKeys(error.fieldErrors));
                _message.setValue(new Event<>(error.userMessage));
            }
        });
    }

    // ---------- 저장 ----------
    public void save() {
        ArchiveItemDto target = _target.getValue();
        if (target == null) return;
        NoteForm f = f();
        // 최종 검증은 서버가 하지만, 필수값은 미리 안내한다.
        Map<String, String> errors = new HashMap<>();
        if (f.name == null || f.name.trim().isEmpty()) errors.put("dessertName", "디저트 이름을 입력해 주세요.");
        if (f.category == null || f.category.trim().isEmpty()) errors.put("category", "카테고리를 선택해 주세요.");
        if (f.rating <= 0f) errors.put("rating", "별점을 남겨 주세요.");
        if (!errors.isEmpty()) {
            _fieldErrors.setValue(errors);
            return;
        }
        _saving.setValue(true);
        _fieldErrors.setValue(new HashMap<String, String>());

        TastingRequest req = new TastingRequest();
        req.dessertName = f.name.trim();
        req.category = f.category;
        req.rating = f.rating;
        req.visitDate = f.tastedDate.toString();
        req.cafeId = parseCafeId(f.place);            // 장소를 고르지 않았으면 0 (서버에서 "장소 없음")
        req.tags = new ArrayList<>(f.tags);
        req.content = f.note;
        req.keepImages = new ArrayList<>(f.existingPhotoUrls);
        req.shareToCommunity = f.share;

        repo.saveTasting(target.id, req, new ArrayList<>(f.newPhotos), new ApiCallback<ArchiveItemDto>() {
            @Override public void onSuccess(ArchiveItemDto data) {
                _saving.setValue(false);
                _saved.setValue(data);
            }
            @Override public void onError(ApiError error) {
                _saving.setValue(false);
                _fieldErrors.setValue(uiKeys(error.fieldErrors));
                _message.setValue(new Event<>(error.userMessage));
            }
        });
    }

    /** 서버 오류의 필드 이름(API 문서의 snake_case)을 화면 입력칸 이름으로 바꾼다 */
    private static Map<String, String> uiKeys(Map<String, String> server) {
        Map<String, String> out = new HashMap<>();
        if (server == null) return out;
        for (Map.Entry<String, String> e : server.entrySet()) {
            String k = e.getKey();
            if ("dessert_name".equals(k)) k = "dessertName";
            else if ("content".equals(k)) k = "note";
            else if ("visit_date".equals(k)) k = "tastedDate";
            else if ("cafe_id".equals(k)) k = "placeId";
            else if ("images".equals(k)) k = "photos";
            out.put(k, e.getValue());
        }
        return out;
    }

    private static int parseCafeId(PlaceDto place) {
        if (place == null || place.id == null || place.id.isEmpty()) return 0;
        try { return Integer.parseInt(place.id.trim()); } catch (NumberFormatException e) { return 0; }
    }

    @Override protected void onCleared() {
        cancelRemoval();
        handler.removeCallbacksAndMessages(null);
        if (tagCall != null) tagCall.cancel();
        if (placeCall != null) placeCall.cancel();
        if (categoryCall != null) categoryCall.cancel();
    }
}
