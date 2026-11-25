package com.example.dessertrip.ui.archive.collect;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import com.example.dessertrip.ui.collect.data.PlaceDto;
import com.example.dessertrip.databinding.ItemPlaceBinding;
import com.example.dessertrip.databinding.SheetPlaceSearchBinding;
import com.example.dessertrip.ui.collect.common.ViewModels;
import com.example.dessertrip.ui.archive.sticker.RegisterFlowViewModel;

import java.util.ArrayList;
import java.util.List;

/** "장소 선택" 바텀시트 — 검색(서버 /places) → 지도 미리보기 → 결과 선택 → 적용 */
public class PlaceSearchSheet extends BottomSheetDialogFragment {
    private SheetPlaceSearchBinding b;
    private RegisterFlowViewModel vm;
    private PlaceAdapter adapter;
    private PlaceDto pending;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        b = SheetPlaceSearchBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        vm = ViewModels.register(requireParentFragment());
        if (vm.form.getValue() != null) pending = vm.form.getValue().place;

        adapter = new PlaceAdapter();
        b.placeList.setLayoutManager(new LinearLayoutManager(requireContext()));
        b.placeList.setAdapter(adapter);
        renderMap();

        b.queryInput.setText(vm.getPlaceQuery());
        b.queryInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int c, int d) { }
            @Override public void onTextChanged(CharSequence s, int a, int c, int d) { }
            @Override public void afterTextChanged(Editable e) { vm.setPlaceQuery(e.toString()); updateEmpty(); }
        });
        b.cancelButton.setOnClickListener(v -> dismiss());
        b.applyButton.setOnClickListener(v -> {
            vm.setPlace(pending);
            dismiss();
        });

        vm.places.observe(getViewLifecycleOwner(), list -> { adapter.submit(list); updateEmpty(); });
        vm.placesLoading.observe(getViewLifecycleOwner(), l -> {
            b.placeProgress.setVisibility(Boolean.TRUE.equals(l) ? View.VISIBLE : View.GONE);
            updateEmpty();
        });
    }

    private void updateEmpty() {
        if (b == null) return;
        boolean loading = Boolean.TRUE.equals(vm.placesLoading.getValue());
        boolean empty = vm.places.getValue() == null || vm.places.getValue().isEmpty();
        b.placeEmpty.setVisibility(empty && !loading ? View.VISIBLE : View.GONE);
        b.placeEmpty.setText(vm.getPlaceQuery().isEmpty() ? "검색어를 입력해 주세요." : "검색 결과가 없어요.");
    }

    /** 지도 SDK 도입 전 자리표시자: 선택한 장소 이름만 표시 (sheet_place_search.xml 의 TODO 참고) */
    private void renderMap() {
        b.mapLabel.setVisibility(pending == null ? View.GONE : View.VISIBLE);
        b.mapHint.setVisibility(pending == null ? View.VISIBLE : View.GONE);
        if (pending != null) b.mapLabel.setText(pending.name);
    }

    @Override public void onDestroyView() { super.onDestroyView(); b = null; }

    private class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.VH> {
        private final List<PlaceDto> data = new ArrayList<>();

        void submit(List<PlaceDto> list) {
            data.clear();
            if (list != null) data.addAll(list);
            notifyDataSetChanged();
        }

        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(ItemPlaceBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override public void onBindViewHolder(@NonNull VH h, int position) {
            final PlaceDto p = data.get(position);
            h.b.name.setText(p.name);
            h.b.address.setText(p.address);
            h.b.address.setVisibility(p.address == null || p.address.isEmpty() ? View.GONE : View.VISIBLE);
            h.b.check.setVisibility(pending != null && p.id.equals(pending.id) ? View.VISIBLE : View.INVISIBLE);
            h.b.getRoot().setOnClickListener(v -> {
                pending = p;
                notifyDataSetChanged();
                renderMap();
            });
        }

        @Override public int getItemCount() { return data.size(); }

        class VH extends RecyclerView.ViewHolder {
            final ItemPlaceBinding b;
            VH(ItemPlaceBinding b) { super(b.getRoot()); this.b = b; }
        }
    }
}
