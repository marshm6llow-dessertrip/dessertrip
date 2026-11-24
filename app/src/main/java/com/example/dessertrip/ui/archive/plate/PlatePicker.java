package com.example.dessertrip.ui.archive.plate;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.example.dessertrip.R;
import com.example.dessertrip.databinding.DialogPlatePickerBinding;
import com.example.dessertrip.databinding.ItemPlatePickerBinding;
import com.example.dessertrip.ui.collect.data.ArchiveItemDto;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * "플레이트에 스티커를 추가해요" 대화상자.
 * 스티커를 눌러 접시에 올릴 것을 고르고(분홍색 = 선택됨) "적용하고 닫기"를 누르면 저장한다.
 * 테두리 색: 노란색 = 내 스티커, 파란색 = 받은 스티커.
 */
final class PlatePicker {
    private PlatePicker() { }

    static AlertDialog show(Fragment f, final PlateViewModel vm) {
        final Context ctx = f.requireContext();
        final DialogPlatePickerBinding d = DialogPlatePickerBinding.inflate(f.getLayoutInflater());
        final Set<Long> selected = new LinkedHashSet<>(vm.placedIds());
        final Adapter adapter = new Adapter(ctx, selected);

        d.recycler.setLayoutManager(new GridLayoutManager(ctx, 2));
        d.recycler.setAdapter(adapter);
        final Runnable refresh = () -> {
            List<ArchiveItemDto> list = vm.pickerItems(d.checkMine.isChecked(), d.checkReceived.isChecked());
            adapter.submit(list);
            d.emptyText.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
        };
        d.checkMine.setOnCheckedChangeListener((v, checked) -> refresh.run());
        d.checkReceived.setOnCheckedChangeListener((v, checked) -> refresh.run());
        refresh.run();

        final AlertDialog dialog = new AlertDialog.Builder(ctx).setView(d.getRoot()).create();
        d.applyButton.setOnClickListener(v -> {
            vm.save(new ArrayList<>(selected));
            dialog.dismiss();
        });
        dialog.show();

        Window w = dialog.getWindow();
        if (w != null) {
            DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            w.setLayout((int) (dm.widthPixels * 0.92f), (int) (dm.heightPixels * 0.66f));
        }
        return dialog;
    }

    private static class Adapter extends RecyclerView.Adapter<Adapter.VH> {
        private final Context ctx;
        private final Set<Long> selected;
        private final List<ArchiveItemDto> data = new ArrayList<>();

        Adapter(Context ctx, Set<Long> selected) { this.ctx = ctx; this.selected = selected; }

        void submit(List<ArchiveItemDto> list) {
            data.clear();
            data.addAll(list);
            notifyDataSetChanged();
        }

        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(ItemPlatePickerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override public void onBindViewHolder(@NonNull VH h, int position) {
            final ArchiveItemDto s = data.get(position);
            boolean isSel = selected.contains(s.stickerId);
            h.b.card.setCardBackgroundColor(ContextCompat.getColor(ctx, isSel ? R.color.blush : R.color.cream_dark));
            h.b.card.setStrokeColor(ContextCompat.getColor(ctx, s.gift ? R.color.received_stroke : R.color.gold));
            h.b.name.setText(s.name == null || s.name.isEmpty() ? "이름 없는 디저트" : s.name);
            h.b.sub.setText(s.gift ? "받은 스티커" : s.placeName == null ? "장소 미지정" : s.placeName);
            Glide.with(h.b.image).load(s.stickerUrl).into(h.b.image);
            h.b.card.setOnClickListener(v -> {
                if (selected.contains(s.stickerId)) {
                    selected.remove(s.stickerId);
                } else if (selected.size() >= PlateViewModel.MAX_STICKERS) {
                    Toast.makeText(ctx, "접시에는 스티커를 " + PlateViewModel.MAX_STICKERS + "개까지 올릴 수 있어요.", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    selected.add(s.stickerId);
                }
                notifyItemChanged(h.getBindingAdapterPosition());
            });
        }

        @Override public int getItemCount() { return data.size(); }

        static class VH extends RecyclerView.ViewHolder {
            final ItemPlatePickerBinding b;
            VH(ItemPlatePickerBinding b) { super(b.getRoot()); this.b = b; }
        }
    }
}
