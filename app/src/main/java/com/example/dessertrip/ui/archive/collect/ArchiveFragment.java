package com.example.dessertrip.ui.archive.collect;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.Toast;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;

import com.example.dessertrip.R;
import com.example.dessertrip.ui.collect.core.network.ApiError;
import com.example.dessertrip.ui.collect.core.network.UploadPolicy;
import com.example.dessertrip.ui.collect.data.ArchiveItemDto;
import com.example.dessertrip.ui.collect.data.PlateDto;
import com.example.dessertrip.databinding.DialogArchiveDetailBinding;
import com.example.dessertrip.databinding.FragmentArchiveBinding;
import com.example.dessertrip.databinding.ItemPhotoThumbBinding;
import com.example.dessertrip.ui.collect.core.util.RatingLabel;
import com.example.dessertrip.ui.archive.plate.PlateFragment;
import com.example.dessertrip.ui.collect.common.ViewModels;
import com.example.dessertrip.ui.record.RecordActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ArchiveFragment extends Fragment {
    private FragmentArchiveBinding b;
    private ArchiveViewModel vm;
    private ArchiveAdapter adapter;
    private Dialog detailDialog;
    private DialogArchiveDetailBinding db;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        b = FragmentArchiveBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        vm = ViewModels.of(this, ArchiveViewModel.class);

        adapter = new ArchiveAdapter(vm::select);
        b.recycler.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        b.recycler.setAdapter(adapter);
        // 플레이트 화면: 하단 탭은 그대로 두고 화면만 바꾼다 (뒤로 가면 도감으로 돌아온다)
        b.plateBanner.setOnClickListener(v -> requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new PlateFragment()).addToBackStack(null).commit());
        b.retryButton.setOnClickListener(v -> vm.refresh());
        // 플레이트 배너: 스크롤 중에는 화면 아래에 붙어 있고, 맨 아래까지 내리면 목록 끝에 놓인다
        b.scroll.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, x, y, oldX, oldY) -> floatBanner());
        b.scroll.addOnLayoutChangeListener((v, l, tp, r, bt, ol, ot, or, ob) -> floatBanner());
        b.plateBanner.addOnLayoutChangeListener((v, l, tp, r, bt, ol, ot, or, ob) -> floatBanner());

        vm.loading.observe(getViewLifecycleOwner(), l -> updateState());
        vm.error.observe(getViewLifecycleOwner(), e -> updateState());
        vm.plate.observe(getViewLifecycleOwner(), this::renderPlate);
        vm.items.observe(getViewLifecycleOwner(), list -> renderList());
        vm.newestFirst.observe(getViewLifecycleOwner(), n -> renderList());
        b.sortButton.setOnClickListener(v -> vm.toggleSort());
        vm.category.observe(getViewLifecycleOwner(), c -> renderList());
        vm.selected.observe(getViewLifecycleOwner(), this::showDetail);
        vm.deleted.observe(getViewLifecycleOwner(), e -> {
            if (e != null && e.getContentIfNotHandled() != null) {
                Toast.makeText(requireContext(), "스티커를 삭제했어요.", Toast.LENGTH_SHORT).show();
            }
        });
        ViewModels.showMessages(this, vm.message);
    }

    @Override public void onResume() {
        super.onResume();
        vm.refresh(); // 기록/플레이트 수정 후 돌아왔을 때 최신화
    }

    /** 배너의 자연스러운 자리가 화면 아래보다 더 아래에 있으면, 그만큼 위로 끌어올려 화면 아래에 붙인다 */
    private void floatBanner() {
        int margin = (int) (12 * getResources().getDisplayMetrics().density);
        int viewportBottom = b.scroll.getScrollY() + b.scroll.getHeight() - margin;
        float lift = Math.max(0, b.plateBanner.getBottom() - viewportBottom);
        b.plateBanner.setTranslationY(-lift);
        // 떠 있을 때(끌어올린 상태)만 그림자를 주고, 맨 아래에서 제자리에 놓이면 그림자를 없앤다
        b.plateBanner.setCardElevation(lift > 0 ? 6 * getResources().getDisplayMetrics().density : 0f);
    }

    private void updateState() {
        boolean loading = Boolean.TRUE.equals(vm.loading.getValue());
        ApiError error = vm.error.getValue();
        b.loadingSpinner.setVisibility(loading ? View.VISIBLE : View.GONE);
        b.errorLayout.setVisibility(!loading && error != null ? View.VISIBLE : View.GONE);
        if (error != null) b.errorText.setText(error.userMessage);
        b.scroll.setVisibility(!loading && error == null ? View.VISIBLE : View.GONE);
    }

    private void renderPlate(PlateDto plate) {
        b.title.setText((plate == null ? "" : plate.nickname) + "의 디저트 도감");
        ArchiveHeader.bindLevelCard(b.levelCard, plate);
    }

    private void renderList() {
        List<ArchiveItemDto> all = vm.items.getValue();
        if (all == null) all = new ArrayList<>();
        String cat = vm.category.getValue();

        List<String> categories = new ArrayList<>();
        List<ArchiveItemDto> visible = new ArrayList<>();
        for (ArchiveItemDto i : all) {
            categories.add(i.category);
            if (cat == null || cat.equals(i.category)) visible.add(i);
        }
        ArchiveHeader.bindChips(b.chips, getLayoutInflater(), cat, ArchiveHeader.countByCategory(categories), all.size(), vm::selectCategory);

        // 최근 수집순 = 번호가 큰(나중에 만든) 것부터
        final boolean newestFirst = !Boolean.FALSE.equals(vm.newestFirst.getValue());
        Collections.sort(visible, (x, y) -> newestFirst ? Integer.compare(y.number, x.number) : Integer.compare(x.number, y.number));
        b.sortButton.setText(newestFirst ? "최근 수집순" : "오래된 순");
        adapter.submit(visible);
        updateState();   // 목록을 그린 뒤에는 로딩 표시가 남지 않게 다시 정리
    }

    // ---------- 상세 다이얼로그 ----------
    private void showDetail(final ArchiveItemDto item) {
        if (item == null) {
            if (detailDialog != null) {
                Dialog d = detailDialog;
                detailDialog = null;
                d.dismiss();
            }
            return;
        }
        if (detailDialog == null) {
            db = DialogArchiveDetailBinding.inflate(getLayoutInflater());
            detailDialog = new Dialog(requireContext());
            detailDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            detailDialog.setContentView(db.getRoot());
            Window w = detailDialog.getWindow();
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            DisplayMetrics dm = getResources().getDisplayMetrics();
            w.setLayout((int) (dm.widthPixels * 0.92f), ViewGroup.LayoutParams.WRAP_CONTENT);
            detailDialog.setOnDismissListener(d -> {
                if (detailDialog != null) { // 사용자가 직접 닫은 경우
                    detailDialog = null;
                    vm.dismissDetail();
                }
            });
            detailDialog.show();
        }
        bindDetail(item);
    }

    /** 노트가 있으면 별점/태그/노트/사진을, 없으면 "테이스팅 노트 추가하기"를 보여준다. */
    private void bindDetail(final ArchiveItemDto item) {
        db.numberText.setText(String.format(Locale.KOREA, "No. %03d", item.number));
        Glide.with(this).load(item.stickerUrl).into(db.detailSticker);
        db.detailName.setText(item.name == null || item.name.isEmpty() ? "이름 없는 디저트" : item.name);
        db.detailPlace.setText(item.placeName == null ? "장소 미지정" : item.placeName);

        float rating = item.rating == null ? 0f : item.rating;
        db.detailStars.setRating(rating);
        db.detailRatingLabel.setText(item.rating == null ? "아직 별점이 없어요"
                : String.format(Locale.KOREA, "%.1f · %s", rating, RatingLabel.of(rating)));

        boolean hasCategory = item.category != null && !item.category.isEmpty();
        db.detailCategory.setText(hasCategory ? item.category : "카테고리 없음");

        db.detailTags.removeAllViews();
        for (String tag : item.tagsSafe()) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.item_tag_chip, db.detailTags, false);
            chip.setText("#" + tag);
            chip.setCheckable(false);
            chip.setChipBackgroundColorResource(R.color.blush);   // 상세에서는 연한 분홍 알약
            chip.setChipStrokeWidth(0f);
            chip.setTextColor(requireContext().getColor(R.color.ink));
            chip.setTextSize(11f);
            db.detailTags.addView(chip);
        }

        boolean noteEmpty = item.note == null || item.note.trim().isEmpty();
        db.detailNote.setText(noteEmpty ? "작성된 테이스팅 노트가 없어요. 지금 남겨볼까요?" : item.note);
        db.detailNote.setTextColor(requireContext().getColor(noteEmpty ? R.color.ink_sub : R.color.ink));

        List<String> photos = item.photosSafe();
        db.photosLabel.setVisibility(photos.isEmpty() ? View.GONE : View.VISIBLE);
        db.photosRow.setVisibility(photos.isEmpty() ? View.GONE : View.VISIBLE);
        String count = "  " + photos.size() + " / " + UploadPolicy.MAX_PHOTOS;
        SpannableString label = new SpannableString("그 날의 사진" + count);
        int start = "그 날의 사진".length();
        label.setSpan(new ForegroundColorSpan(requireContext().getColor(R.color.ink_sub)), start, label.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        label.setSpan(new RelativeSizeSpan(0.8f), start, label.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        db.photosLabel.setText(label);
        db.photosRow.removeAllViews();
        for (int i = 0; i < Math.min(4, photos.size()); i++) {
            ItemPhotoThumbBinding t = ItemPhotoThumbBinding.inflate(getLayoutInflater(), db.photosRow, false);
            t.remove.setVisibility(View.GONE);
            Glide.with(this).load(photos.get(i)).into(t.image);
            db.photosRow.addView(t.getRoot());
        }

        db.actionButton.setText(item.hasNote() ? "기록 수정하기" : "테이스팅 노트 추가하기");
        db.actionButton.setOnClickListener(v -> {
            // 이미 쓴 노트를 고치는 경우에만 확인 팝업을 띄운다 (처음 쓰는 경우는 바로 이동)
            if (item.hasNote()) confirmEdit(item); else openNote(item);
        });
        db.deleteNoteText.setVisibility(item.hasNote() ? View.VISIBLE : View.GONE);   // 지울 노트가 있을 때만
        db.deleteNoteText.setOnClickListener(v -> confirmDeleteNote(item));
        db.deleteText.setOnClickListener(v -> confirmDelete(item));
    }

    /** 기존 항목의 노트 작성/수정: RecordActivity 를 노트 화면부터 시작한다 */
    private void openNote(ArchiveItemDto item) {
        vm.dismissDetail();
        Intent intent = new Intent(requireContext(), RecordActivity.class);
        intent.putExtra(RecordActivity.EXTRA_STICKER_ID, item.id);
        startActivity(intent);
    }

    /** 아래쪽에 아니오/예 버튼이 함께 보이는 확인 팝업 */
    private void confirm(String message, Runnable onYes) {
        new AlertDialog.Builder(requireContext())
                .setMessage(message)
                .setNegativeButton("아니오", null)
                .setPositiveButton("예", (d, which) -> onYes.run())
                .show();
    }

    private void confirmEdit(final ArchiveItemDto item) {
        confirm("테이스팅 노트를 수정하시겠습니까?\n주의 : 게시글에 올린 테이스팅 노트는 수정되지 않습니다.", () -> openNote(item));
    }

    /** 노트만 지우고 스티커는 남는다 */
    private void confirmDeleteNote(final ArchiveItemDto item) {
        confirm("테이스팅 노트를 삭제하시겠습니까?\n주의 : 게시글에 올린 테이스팅 노트는 삭제되지 않습니다.", () -> vm.deleteNote(item.id));
    }

    /** 스티커와 테이스팅 노트를 모두 지운다 */
    private void confirmDelete(final ArchiveItemDto item) {
        confirm("스티커를 삭제하시겠습니까?\n주의 : 스티커와 테이스팅 노트가 모두 삭제됩니다. 게시글에 올린 테이스팅 노트는 삭제되지 않습니다.", () -> vm.delete(item.id));
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        if (detailDialog != null) { // ViewModel 의 selected 는 유지(회전 후 다시 표시)
            Dialog d = detailDialog;
            detailDialog = null;
            d.dismiss();
        }
        b = null;
    }
}
