package com.example.dessertrip.ui.archive.collect;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.dessertrip.ui.collect.core.network.ApiCallback;
import com.example.dessertrip.ui.collect.core.network.ApiError;
import com.example.dessertrip.ui.collect.core.util.Event;
import com.example.dessertrip.ui.collect.data.ArchiveItemDto;
import com.example.dessertrip.ui.collect.data.DessertRepository;
import com.example.dessertrip.ui.collect.data.PlateDto;

import java.util.ArrayList;
import java.util.List;

public class ArchiveViewModel extends ViewModel {
    private final DessertRepository repo;

    private final MutableLiveData<Boolean> _loading = new MutableLiveData<>(true);
    private final MutableLiveData<PlateDto> _plate = new MutableLiveData<>();
    // 첫 응답이 오기 전에는 null (빈 도감과 '아직 못 불러온 상태'를 구분해야 NEW 표시가 정확하다)
    private final MutableLiveData<List<ArchiveItemDto>> _items = new MutableLiveData<>();
    private final MutableLiveData<String> _category = new MutableLiveData<>(null);
    private final MutableLiveData<ArchiveItemDto> _selected = new MutableLiveData<>();
    private final MutableLiveData<ApiError> _error = new MutableLiveData<>();       // 첫 로딩 실패(전체 화면 오류)
    private final MutableLiveData<Event<String>> _message = new MutableLiveData<>(); // 일회성 메시지(스낵바)
    private final MutableLiveData<Event<Boolean>> _deleted = new MutableLiveData<>(); // 삭제 성공(토스트 표시용)
    private final MutableLiveData<Boolean> _newestFirst = new MutableLiveData<>(true);   // 최근 수집순(true) / 오래된 순(false)
    private boolean deleting;
    private boolean loadedOnce;      // 한 번이라도 목록을 받았으면 이후 갱신은 스피너 없이
    private int refreshRun;          // 오래된 응답이 로딩 상태를 되돌리지 못하게

    public final LiveData<Boolean> loading = _loading;
    public final LiveData<PlateDto> plate = _plate;
    public final LiveData<List<ArchiveItemDto>> items = _items;
    public final LiveData<String> category = _category;
    public final LiveData<ArchiveItemDto> selected = _selected;
    public final LiveData<ApiError> error = _error;
    public final LiveData<Event<String>> message = _message;
    public final LiveData<Event<Boolean>> deleted = _deleted;
    public final LiveData<Boolean> newestFirst = _newestFirst;

    public ArchiveViewModel(DessertRepository repo) {
        this.repo = repo;
        refresh();
    }

    /** 화면 복귀(onResume) 때마다 호출 — 이미 데이터가 있으면 로딩 스피너 없이 갱신 */
    public void refresh() {
        final boolean empty = !loadedOnce;
        final int run = ++refreshRun;
        _loading.setValue(empty);
        _error.setValue(null);

        repo.getPlate(new ApiCallback<PlateDto>() {
            @Override public void onSuccess(PlateDto data) { _plate.setValue(data); }
            @Override public void onError(ApiError error) { /* 도감 목록 오류에서 안내 */ }
        });
        repo.getArchive(new ApiCallback<List<ArchiveItemDto>>() {
            @Override public void onSuccess(List<ArchiveItemDto> data) {
                if (run != refreshRun) return;     // 더 최신 요청이 있으면 이 응답은 버림
                loadedOnce = true;
                _loading.setValue(false);
                // 선물로 받은 스티커는 도감에 보이지 않고, 플레이트 선택 창에만 나온다
                List<ArchiveItemDto> mine = new ArrayList<>();
                if (data != null) for (ArchiveItemDto i : data) if (!i.gift) mine.add(i);
                _items.setValue(mine);
            }
            @Override public void onError(ApiError error) {
                if (run != refreshRun) return;
                _loading.setValue(false);
                if (empty) _error.setValue(error);
                else _message.setValue(new Event<>(error.userMessage));
            }
        });
    }

    public void selectCategory(String c) { _category.setValue(c); }

    public void toggleSort() { _newestFirst.setValue(!Boolean.TRUE.equals(_newestFirst.getValue())); }

    public void select(final ArchiveItemDto item) {
        _selected.setValue(item);
        // 목록에는 없는 상세(노트/사진/태그)를 불러온다
        repo.getArchiveItem(item.id, new ApiCallback<ArchiveItemDto>() {
            @Override public void onSuccess(ArchiveItemDto data) {
                ArchiveItemDto s = _selected.getValue();
                if (s != null && s.id == item.id && data != null) _selected.setValue(data);
            }
            @Override public void onError(ApiError error) { _message.setValue(new Event<>(error.userMessage)); }
        });
    }

    public void dismissDetail() { _selected.setValue(null); }

    /** 테이스팅 노트만 삭제. 스티커는 남고, 상세와 목록을 새로 고친다. */
    public void deleteNote(final long id) {
        if (deleting) return;
        deleting = true;
        repo.deleteNote(id, new ApiCallback<ArchiveItemDto>() {
            @Override public void onSuccess(ArchiveItemDto data) {
                deleting = false;
                ArchiveItemDto s = _selected.getValue();
                if (s != null && s.id == id && data != null) _selected.setValue(data);
                _message.setValue(new Event<>("테이스팅 노트를 삭제했어요."));
                refresh();
            }
            @Override public void onError(ApiError error) {
                deleting = false;
                _message.setValue(new Event<>(error.userMessage));
            }
        });
    }

    /** 스티커(도감 항목) 삭제. 성공하면 목록에서 빼고 상세를 닫은 뒤 deleted 이벤트를 보낸다. */
    public void delete(final long id) {
        if (deleting) return;
        deleting = true;
        repo.deleteArchiveItem(id, new ApiCallback<Void>() {
            @Override public void onSuccess(Void data) {
                deleting = false;
                List<ArchiveItemDto> cur = _items.getValue();
                List<ArchiveItemDto> next = new ArrayList<>();
                if (cur != null) for (ArchiveItemDto i : cur) if (i.id != id) next.add(i);
                _items.setValue(next);
                _selected.setValue(null);
                _deleted.setValue(new Event<>(true));
                refresh();   // 레벨/개수 갱신
            }
            @Override public void onError(ApiError error) {
                deleting = false;
                _message.setValue(new Event<>(error.userMessage));
            }
        });
    }
}
