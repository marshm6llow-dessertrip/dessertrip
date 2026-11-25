package com.example.dessertrip.ui.archive.sticker;

import com.example.dessertrip.ui.archive.collect.NoteForm;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;

import com.example.dessertrip.R;
import com.example.dessertrip.ui.collect.data.ArchiveItemDto;
import com.example.dessertrip.databinding.FragmentStickerBinding;
import com.example.dessertrip.ui.collect.common.ViewModels;
import com.example.dessertrip.ui.archive.collect.CategoryInput;

import java.util.HashMap;
import java.util.Map;

/** 한 화면에서 3단계: 미리보기("스티커로 만들까요?") → 배경 제거 중 → 완료("디저트 스티커 완성!") */
public class StickerFragment extends Fragment {
    private FragmentStickerBinding b;
    private RegisterFlowViewModel vm;
    private NavController nav;
    private OnBackPressedCallback blockBack;
    private boolean rendering;   // 프로그램이 값을 넣는 동안 TextWatcher 무시

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        b = FragmentStickerBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        nav = NavHostFragment.findNavController(this);
        vm = ViewModels.register(this);

        b.topBar.titleText.setText("디저트 등록");
        b.topBar.closeButton.setVisibility(View.INVISIBLE);
        b.topBar.backButton.setOnClickListener(v -> nav.popBackStack());

        // 변환 중에는 뒤로가기 무시
        blockBack = new OnBackPressedCallback(false) {
            @Override public void handleOnBackPressed() {
                // 변환 중: 무시 / 완성 화면: 취소할지 묻는다 (저장하지 않고 나가기)
                if (vm.stage.getValue() == StickerStage.DONE && !Boolean.TRUE.equals(vm.saving.getValue())) confirmCancel();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), blockBack);

        // 완성 단계에서는 이름·카테고리가 필수: 서버에 저장(PATCH)에 성공해야 다음으로 넘어간다.
        b.primaryButton.setOnClickListener(v -> {
            if (vm.stage.getValue() == StickerStage.DONE) vm.submitBasics(true);
            else vm.createSticker();
        });
        b.secondaryButton.setOnClickListener(v -> nav.popBackStack());
        b.stickerOnlyText.setOnClickListener(v -> vm.submitBasics(false));

        b.nameInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) { }
            @Override public void onTextChanged(CharSequence s, int st, int be, int c) { }
            @Override public void afterTextChanged(Editable e) { if (!rendering) vm.setName(e.toString()); }
        });
        CategoryInput.bind(this, b.categoryField, vm);

        vm.pickedUri.observe(getViewLifecycleOwner(), uri -> Glide.with(this).load(uri).into(b.previewImage));
        vm.stage.observe(getViewLifecycleOwner(), this::render);
        vm.target.observe(getViewLifecycleOwner(), (ArchiveItemDto t) -> {
            if (t != null) Glide.with(this).load(t.stickerUrl).into(b.stickerImage);
        });
        vm.form.observe(getViewLifecycleOwner(), this::renderForm);
        vm.fieldErrors.observe(getViewLifecycleOwner(), this::renderErrors);
        vm.saving.observe(getViewLifecycleOwner(), saving -> updateButtons());
        vm.basicsDone.observe(getViewLifecycleOwner(), e -> {
            Boolean thenNote = e == null ? null : e.getContentIfNotHandled();
            if (thenNote == null) return;
            if (thenNote) nav.navigate(R.id.noteFragment);                 // stickerId 기본값(-1): 방금 만든 스티커 이어서 작성
            else com.example.dessertrip.ui.record.RecordActivity.finishToArchive(requireActivity());   // 스티커만 저장 → 도감으로
        });
        vm.discarded.observe(getViewLifecycleOwner(), e -> {
            Boolean go = e == null ? null : e.getContentIfNotHandled();
            if (go != null && go) com.example.dessertrip.ui.record.RecordActivity.finishToArchive(requireActivity());
        });
        ViewModels.showMessages(this, vm.message);
    }

    private void renderForm(NoteForm f) {
        if (f == null || b == null) return;
        rendering = true;
        if (!b.nameInput.getText().toString().equals(f.name)) b.nameInput.setText(f.name);
        rendering = false;
        CategoryInput.show(b.categoryField, f.category);
    }

    private void renderErrors(Map<String, String> errors) {
        if (b == null) return;
        if (errors == null) errors = new HashMap<>();
        b.nameLayout.setError(errors.get("dessertName"));
        String c = errors.get("category");
        b.categoryError.setText(c);
        b.categoryError.setVisibility(c == null ? View.GONE : View.VISIBLE);
    }

    /** "스티커 만들기를 취소할까요?" — 예: 스티커를 저장하지 않고 도감으로, 아니오: 계속 작성 */
    private void confirmCancel() {
        new AlertDialog.Builder(requireContext())
                .setTitle("스티커 만들기를 취소할까요?")
                .setMessage("저장하지 않은 스티커는 사라져요.")
                .setNegativeButton("아니오", null)
                .setPositiveButton("예", (d, w) -> vm.discardCreated())
                .show();
    }

    private void updateButtons() {
        if (b == null) return;
        boolean loading = vm.stage.getValue() == StickerStage.LOADING;
        boolean saving = Boolean.TRUE.equals(vm.saving.getValue());
        b.primaryButton.setEnabled(!loading && !saving);
        b.stickerOnlyText.setEnabled(!saving);
    }

    private void render(StickerStage stage) {
        boolean loading = stage == StickerStage.LOADING;
        boolean done = stage == StickerStage.DONE;
        blockBack.setEnabled(loading || done);
        b.loadingOverlay.setVisibility(loading ? View.VISIBLE : View.GONE);
        b.topBar.backButton.setVisibility(done || loading ? View.INVISIBLE : View.VISIBLE);

        b.titleText.setText(done ? "디저트 스티커 완성!" : "해당 이미지를 스티커로 만들까요?");
        b.doneSub.setVisibility(done ? View.VISIBLE : View.GONE);
        b.previewImage.setVisibility(done ? View.GONE : View.VISIBLE);
        b.stickerImage.setVisibility(done ? View.VISIBLE : View.GONE);
        b.badge.setVisibility(done ? View.VISIBLE : View.GONE);
        b.captionText.setText(done ? "이름과 카테고리는 꼭 입력해 주세요.\n맛과 향까지 기록하면 더 오래 기억할 수 있어요." : "사진 속 디저트만 쏙, 배경은 깔끔하게 지워요.");

        b.basicsForm.setVisibility(done ? View.VISIBLE : View.GONE);
        b.primaryButton.setText(done ? "테이스팅 노트 추가하기" : "이 사진 스티커로 만들기");
        updateButtons();
        b.secondaryButton.setVisibility(done ? View.GONE : View.VISIBLE);
        b.stickerOnlyText.setVisibility(done ? View.VISIBLE : View.GONE);
    }

    @Override public void onDestroyView() { super.onDestroyView(); b = null; }
}
