package com.example.dessertrip.ui.archive.plate;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.dessertrip.ui.collect.core.network.ApiCallback;
import com.example.dessertrip.ui.collect.core.network.ApiError;
import com.example.dessertrip.ui.collect.core.util.Event;
import com.example.dessertrip.ui.collect.data.ArchiveItemDto;
import com.example.dessertrip.ui.collect.data.DessertRepository;
import com.example.dessertrip.ui.collect.data.PlateDto;
import com.example.dessertrip.ui.collect.data.PlateResponseDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 플레이트 화면: 위쪽 카드(레벨), 접시 위 스티커, 스티커 선택 목록을 함께 불러오고 저장한다. */
public class PlateViewModel extends ViewModel {
    /** 접시에 올릴 수 있는 스티커 수 (서버 규칙과 같은 값) */
    public static final int MAX_STICKERS = 8;

    /** 접시 위 스티커 한 개 (위치는 화면이 접시 크기에 맞춰 무작위로 정한다) */
    public static class Placed {
        public final long stickerId;
        public final String imageUrl;
        public final String name;
        public final String category;
        public final boolean gift;      // 친구에게 선물받은 스티커
        Placed(long stickerId, String imageUrl, String name, String category, boolean gift) {
            this.stickerId = stickerId; this.imageUrl = imageUrl; this.name = name; this.category = category; this.gift = gift;
        }
    }

    private final DessertRepository repo;
    private final MutableLiveData<Boolean> _loading = new MutableLiveData<>(true);
    private final MutableLiveData<ApiError> _error = new MutableLiveData<>();
    private final MutableLiveData<PlateDto> _plate = new MutableLiveData<>();
    private final MutableLiveData<List<Placed>> _placed = new MutableLiveData<>(new ArrayList<Placed>());
    private final MutableLiveData<String> _category = new MutableLiveData<>(null);   // null 이면 전체
    private final MutableLiveData<Event<String>> _message = new MutableLiveData<>();

    private List<ArchiveItemDto> items = new ArrayList<>();      // 내가 가진 모든 스티커(선물 받은 것 포함)
    private PlateResponseDto layout = new PlateResponseDto();    // 접시 위 스티커
    private int pending;
    private ApiError firstError;
    private boolean saving;

    public final LiveData<Boolean> loading = _loading;
    public final LiveData<ApiError> error = _error;
    public final LiveData<PlateDto> plate = _plate;
    public final LiveData<List<Placed>> placed = _placed;
    public final LiveData<String> category = _category;
    public final LiveData<Event<String>> message = _message;

    public PlateViewModel(DessertRepository repo) {
        this.repo = repo;
        load();
    }

    /** 레벨 카드 + 스티커 목록 + 접시 위 스티커를 한꺼번에 불러온다 */
    public void load() {
        _loading.setValue(true);
        _error.setValue(null);
        pending = 3;
        firstError = null;
        repo.getPlate(new ApiCallback<PlateDto>() {
            @Override public void onSuccess(PlateDto data) { _plate.setValue(data); done(null); }
            @Override public void onError(ApiError error) { done(error); }
        });
        repo.getArchive(new ApiCallback<List<ArchiveItemDto>>() {
            @Override public void onSuccess(List<ArchiveItemDto> data) {
                items = data == null ? new ArrayList<ArchiveItemDto>() : data;
                done(null);
            }
            @Override public void onError(ApiError error) { done(error); }
        });
        repo.getPlateStickers(new ApiCallback<PlateResponseDto>() {
            @Override public void onSuccess(PlateResponseDto data) { layout = safe(data); done(null); }
            @Override public void onError(ApiError error) { done(error); }
        });
    }

    private static PlateResponseDto safe(PlateResponseDto d) {
        PlateResponseDto r = d == null ? new PlateResponseDto() : d;
        if (r.stickers == null) r.stickers = new ArrayList<>();
        return r;
    }

    private void done(ApiError e) {
        if (e != null && firstError == null) firstError = e;
        if (--pending > 0) return;
        _loading.setValue(false);
        _error.setValue(firstError);
        buildPlaced();
    }

    /** 접시 위 스티커에 도감 정보(이름, 선물 여부)를 붙인다 */
    private void buildPlaced() {
        Map<Long, ArchiveItemDto> byId = new HashMap<>();
        for (ArchiveItemDto i : items) byId.put(i.stickerId, i);

        List<Placed> out = new ArrayList<>();
        for (PlateResponseDto.Item s : layout.stickers) {
            ArchiveItemDto it = byId.get(s.stickerId);
            String url = s.stickerImgUrl != null ? s.stickerImgUrl : it == null ? null : it.stickerUrl;
            out.add(new Placed(s.stickerId, url, it == null ? "" : it.name, it == null ? null : it.category, it != null && it.gift));
        }
        _placed.setValue(out);
    }

    public void selectCategory(String c) { _category.setValue(c); }

    // ---------- 스티커 선택 목록 ----------

    /** 선택 목록에 보여 줄 스티커 (내 스티커 / 받은 스티커 필터, 최근 수집순) */
    public List<ArchiveItemDto> pickerItems(boolean mine, boolean received) {
        List<ArchiveItemDto> out = new ArrayList<>();
        for (ArchiveItemDto i : items) if (i.gift ? received : mine) out.add(i);
        java.util.Collections.sort(out, (x, y) -> Long.compare(y.stickerId, x.stickerId));   // 나중에 만든 것이 먼저
        return out;
    }

    /** 지금 접시 위에 있는 스티커 id (놓인 순서 유지) */
    public Set<Long> placedIds() {
        Set<Long> ids = new LinkedHashSet<>();
        for (PlateResponseDto.Item s : layout.stickers) ids.add(s.stickerId);
        return ids;
    }

    /** "적용하고 닫기": 선택한 스티커로 접시 구성을 바꾼다 (위치는 서버가 정하고, 화면은 접시에 맞춰 다시 배치한다) */
    public void save(final List<Long> stickerIds) {
        if (saving) return;
        saving = true;
        repo.savePlate(stickerIds, new ApiCallback<PlateResponseDto>() {
            @Override public void onSuccess(PlateResponseDto data) {
                // 저장 응답에는 이미지 주소가 없으므로 접시 위 스티커를 다시 받아 온다
                repo.getPlateStickers(new ApiCallback<PlateResponseDto>() {
                    @Override public void onSuccess(PlateResponseDto fresh) {
                        saving = false;
                        layout = safe(fresh);
                        buildPlaced();
                    }
                    @Override public void onError(ApiError error) { saving = false; _message.setValue(new Event<>(error.userMessage)); }
                });
            }
            @Override public void onError(ApiError error) { saving = false; _message.setValue(new Event<>(error.userMessage)); }
        });
    }

    /** "삭제하기": 접시에서만 빼고 도감의 스티커는 그대로 둔다 */
    public void remove(long stickerId) {
        List<Long> ids = new ArrayList<>(placedIds());
        ids.remove(Long.valueOf(stickerId));
        save(ids);
    }
}
