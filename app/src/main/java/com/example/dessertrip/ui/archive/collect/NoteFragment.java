package com.example.dessertrip.ui.archive.collect;

import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;

import com.example.dessertrip.R;
import com.example.dessertrip.ui.collect.core.network.UploadPolicy;
import com.example.dessertrip.ui.collect.data.ArchiveItemDto;
import com.example.dessertrip.databinding.FragmentNoteBinding;
import com.example.dessertrip.databinding.ItemPhotoThumbBinding;
import com.example.dessertrip.ui.collect.core.util.RatingLabel;
import com.example.dessertrip.ui.collect.common.ViewModels;
import com.example.dessertrip.ui.archive.sticker.RegisterFlowViewModel;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

/** 테이스팅 노트 — 별점/이름/장소/카테고리/태그/노트/사진/공유 */
public class NoteFragment extends Fragment {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy. MM. dd");

    private FragmentNoteBinding b;
    private RegisterFlowViewModel vm;
    private NavController nav;
    private long stickerId = -1L;
    private boolean rendering; // 프로그램이 값을 넣는 동안 TextWatcher/Listener 무시

    private final ActivityResultLauncher<PickVisualMediaRequest> photoPicker =
            registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(UploadPolicy.MAX_PHOTOS),
                    uris -> { if (vm != null) vm.addPhotos(uris); });

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        b = FragmentNoteBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        nav = NavHostFragment.findNavController(this);
        vm = ViewModels.register(this);
        stickerId = getArguments() == null ? -1L : getArguments().getLong("stickerId", -1L);
        if (stickerId > 0) vm.loadExisting(stickerId);

        b.topBar.titleText.setText("테이스팅 노트");
        b.topBar.backButton.setOnClickListener(v -> { if (!nav.popBackStack()) requireActivity().finish(); });
        b.topBar.closeButton.setOnClickListener(v -> requireActivity().finish());

        b.nameInput.addTextChangedListener(new SimpleWatcher() {
            @Override public void onText(String s) { if (!rendering) vm.setName(s); }
        });
        b.noteInput.addTextChangedListener(new SimpleWatcher() {
            @Override public void onText(String s) {
                if (rendering) return;
                vm.setNote(s);
                updateCounter(s.length());
            }
        });
        b.tagSearchInput.addTextChangedListener(new SimpleWatcher() {
            @Override public void onText(String s) { if (!rendering) vm.setTagQuery(s); }
        });

        // 레이팅바: 사용자가 직접 바꿨을 때만 반영한다 (화면 갱신 때 setRating 으로 바뀌는 것은 무시)
        b.starRating.setOnRatingBarChangeListener((bar, rating, fromUser) -> { if (fromUser) vm.setRating(rating); });
        b.shareSwitch.setOnCheckedChangeListener((v, checked) -> { if (!rendering) vm.setShare(checked); });
        b.dateRow.setOnClickListener(v -> showDatePicker());
        b.placeField.setOnClickListener(v -> {
            if (getChildFragmentManager().findFragmentByTag("place") == null) {
                new PlaceSearchSheet().show(getChildFragmentManager(), "place");
            }
        });
        CategoryInput.bind(this, b.categoryField, vm);
        b.saveButton.setOnClickListener(v -> vm.save());

        vm.clearTagInput.observe(getViewLifecycleOwner(), e -> {
            if (e != null && e.getContentIfNotHandled() != null) b.tagSearchInput.setText("");
        });
        vm.refreshTags();   // 다른 화면에서 추가된 태그도 보이도록 서버 목록을 다시 불러온다
        vm.form.observe(getViewLifecycleOwner(), this::render);
        vm.tagCandidates.observe(getViewLifecycleOwner(), c -> renderTags(vm.form.getValue()));
        vm.target.observe(getViewLifecycleOwner(), this::renderTarget);
        vm.fieldErrors.observe(getViewLifecycleOwner(), this::renderErrors);
        vm.saving.observe(getViewLifecycleOwner(), saving -> {
            boolean s = Boolean.TRUE.equals(saving);
            b.saveButton.setText(s ? "저장 중..." : "기록 저장하기");
            b.saveButton.setEnabled(!s && vm.target.getValue() != null);
        });
        vm.saved.observe(getViewLifecycleOwner(), saved -> {
            if (saved != null && nav.getCurrentDestination() != null
                    && nav.getCurrentDestination().getId() == R.id.noteFragment) {
                nav.navigate(R.id.savedFragment, null,
                        new NavOptions.Builder().setPopUpTo(R.id.noteFragment, true).build());
            }
        });
        ViewModels.showMessages(this, vm.message);
    }

    // ---------- 렌더링 ----------
    private void render(NoteForm f) {
        if (f == null || b == null) return;
        rendering = true;
        if (!b.nameInput.getText().toString().equals(f.name)) b.nameInput.setText(f.name);
        if (!b.noteInput.getText().toString().equals(f.note)) b.noteInput.setText(f.note);
        updateCounter(f.note.length());

        b.dateText.setText(f.tastedDate.format(DATE_FORMAT));
        b.starRating.setRating(f.rating);
        b.ratingLabel.setText(f.rating > 0f
                ? String.format(Locale.KOREA, "%.1f · %s", f.rating, RatingLabel.of(f.rating))
                : RatingLabel.of(0f));
        b.placeField.setText(f.place == null ? null : f.place.name);
        CategoryInput.show(b.categoryField, f.category);
        b.shareSwitch.setChecked(f.share);
        rendering = false;

        renderTags(f);
        renderPhotos(f);
    }

    private void updateCounter(int length) {
        b.noteCounter.setText(String.format(Locale.KOREA, "%d / %,d", length, RegisterFlowViewModel.MAX_NOTE_LENGTH));
    }

    private void renderTarget(ArchiveItemDto t) {
        boolean show = stickerId > 0 && t != null;
        b.targetCard.setVisibility(show ? View.VISIBLE : View.GONE);
        b.saveButton.setEnabled(t != null && !Boolean.TRUE.equals(vm.saving.getValue()));
        if (!show) return;
        Glide.with(this).load(t.stickerUrl).into(b.targetImage);
        b.targetNumber.setText(String.format(Locale.KOREA, "NO. %03d · 도감에 저장된 디저트", t.number));
        b.targetName.setText(t.name == null || t.name.isEmpty() ? "이름 없는 디저트" : t.name);
    }

    private void renderTags(NoteForm f) {
        if (f == null || b == null) return;
        b.tagCount.setText(f.tags.size() + "개 선택");
        b.tagGroup.removeAllViews();
        for (final String tag : vm.tagChips()) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.item_tag_chip, b.tagGroup, false);
            chip.setText("#" + tag);
            chip.setCheckable(true);
            chip.setChecked(f.tags.contains(tag));
            chip.setOnClickListener(v -> vm.toggleTag(tag)); // 상태는 form 갱신 후 다시 그려진다
            b.tagGroup.addView(chip);
        }
        // 검색한 태그가 목록에 없으면 새 태그로 추가할 수 있다. (추가하면 서버 태그 목록에 저장되어 다음에도 검색된다)
        if (vm.canAddTag()) {
            final String query = vm.getTagQuery();
            Chip add = (Chip) getLayoutInflater().inflate(R.layout.item_tag_chip, b.tagGroup, false);
            add.setText("+ '" + query + "' 태그 추가");
            add.setCheckable(false);
            add.setOnClickListener(v -> vm.addTag(query));
            b.tagGroup.addView(add);
        }
    }

    private void renderPhotos(NoteForm f) {
        b.photoLabel.setText("오늘의 사진  " + f.photoCount() + " / " + UploadPolicy.MAX_PHOTOS);
        b.photoRow.removeAllViews();
        for (final String url : f.existingPhotoUrls) {
            ItemPhotoThumbBinding t = ItemPhotoThumbBinding.inflate(getLayoutInflater(), b.photoRow, false);
            Glide.with(this).load(url).centerCrop().into(t.image);
            t.remove.setOnClickListener(v -> vm.removeExistingPhoto(url));
            b.photoRow.addView(t.getRoot());
        }
        for (final Uri uri : f.newPhotos) {
            ItemPhotoThumbBinding t = ItemPhotoThumbBinding.inflate(getLayoutInflater(), b.photoRow, false);
            Glide.with(this).load(uri).centerCrop().into(t.image);
            t.remove.setOnClickListener(v -> vm.removeNewPhoto(uri));
            b.photoRow.addView(t.getRoot());
        }
        if (f.photoCount() < UploadPolicy.MAX_PHOTOS) {
            View add = getLayoutInflater().inflate(R.layout.item_photo_add, b.photoRow, false);
            add.setOnClickListener(v -> photoPicker.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build()));
            b.photoRow.addView(add);
        }
    }

    private void renderErrors(Map<String, String> errors) {
        if (errors == null) errors = new java.util.HashMap<>();
        b.nameLayout.setError(errors.get("dessertName"));
        b.noteLayout.setError(errors.get("note"));
        setError(b.categoryError, errors.get("category"));
        setError(b.ratingError, errors.get("rating"));
    }

    private static void setError(android.widget.TextView v, String message) {
        v.setText(message);
        v.setVisibility(message == null ? View.GONE : View.VISIBLE);
    }

    // ---------- 선택 UI ----------
    /** 날짜 선택: dialog_date_picker.xml(스피너 모드 데이트피커)을 인플레이트한 대화상자 */
    private void showDatePicker() {
        NoteForm f = vm.form.getValue();
        View dialogView = View.inflate(requireContext(), R.layout.dialog_date_picker, null);
        final DatePicker datePicker = dialogView.findViewById(R.id.dlgDatePicker);
        datePicker.init(f.tastedDate.getYear(), f.tastedDate.getMonthValue() - 1, f.tastedDate.getDayOfMonth(), null);

        new AlertDialog.Builder(requireContext())
                .setTitle("방문 날짜")
                .setView(dialogView)
                .setPositiveButton("확인", (d, which) -> vm.setDate(LocalDate.of(
                        datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth())))
                .setNegativeButton("취소", null)
                .show();
    }

    @Override public void onDestroyView() { super.onDestroyView(); b = null; }

    private abstract static class SimpleWatcher implements TextWatcher {
        abstract void onText(String s);
        @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) { }
        @Override public void onTextChanged(CharSequence s, int st, int be, int c) { }
        @Override public void afterTextChanged(Editable e) { onText(e.toString()); }
    }
}
