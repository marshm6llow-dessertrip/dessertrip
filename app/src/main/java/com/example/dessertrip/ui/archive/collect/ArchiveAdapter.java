package com.example.dessertrip.ui.archive.collect;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.example.dessertrip.R;
import com.example.dessertrip.ui.collect.data.ArchiveItemDto;
import com.example.dessertrip.databinding.ItemArchiveCardBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ArchiveAdapter extends RecyclerView.Adapter<ArchiveAdapter.VH> {
    public interface OnItemClick { void onClick(ArchiveItemDto item); }

    private final List<ArchiveItemDto> data = new ArrayList<>();
    private final OnItemClick onClick;

    public ArchiveAdapter(OnItemClick onClick) {
        this.onClick = onClick;
    }

    public void submit(List<ArchiveItemDto> list) {
        data.clear();
        data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemArchiveCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override public void onBindViewHolder(@NonNull VH h, int position) {
        final ArchiveItemDto item = data.get(position);
        h.b.number.setText(String.format(Locale.KOREA, "No. %03d", item.number));
        h.b.name.setText(item.name == null || item.name.isEmpty() ? "이름 없는 디저트" : item.name);
        h.b.place.setText(item.placeName == null ? "장소 미지정" : item.placeName);
        // 테이스팅 노트가 아직 없는 스티커는 #8B8078 배경, 작성된 스티커는 기존 배경
        boolean hasNote = item.hasNote();
        h.b.stickerFrame.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(h.b.getRoot().getContext(), hasNote ? R.color.cream_dark : R.color.sticker_empty_bg)));
        h.b.number.setTextColor(ContextCompat.getColor(h.b.getRoot().getContext(), hasNote ? R.color.ink_sub : R.color.white));
        Glide.with(h.b.sticker).load(item.stickerUrl).into(h.b.sticker);
        h.b.getRoot().setOnClickListener(v -> onClick.onClick(item));
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final ItemArchiveCardBinding b;
        VH(ItemArchiveCardBinding b) { super(b.getRoot()); this.b = b; }
    }
}
