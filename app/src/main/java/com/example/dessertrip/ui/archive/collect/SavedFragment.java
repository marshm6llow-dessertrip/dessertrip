package com.example.dessertrip.ui.archive.collect;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;

import com.example.dessertrip.ui.collect.data.ArchiveItemDto;
import com.example.dessertrip.databinding.FragmentSavedBinding;
import com.example.dessertrip.ui.collect.common.ViewModels;
import com.example.dessertrip.ui.archive.sticker.RegisterFlowViewModel;

/** 저장 완료 — 도감으로 복귀 */
public class SavedFragment extends Fragment {
    private FragmentSavedBinding b;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        b = FragmentSavedBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        final NavController nav = NavHostFragment.findNavController(this);
        RegisterFlowViewModel vm = ViewModels.register(this);

        b.topBar.titleText.setText("저장 완료");
        b.topBar.backButton.setVisibility(View.INVISIBLE);
        View.OnClickListener toArchive = v -> com.example.dessertrip.ui.record.RecordActivity.finishToArchive(requireActivity());
        b.topBar.closeButton.setOnClickListener(toArchive);
        b.doneButton.setOnClickListener(toArchive);
        // 시스템 뒤로가기도 도감으로
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { com.example.dessertrip.ui.record.RecordActivity.finishToArchive(requireActivity()); }
        });

        ArchiveItemDto item = vm.saved.getValue() != null ? vm.saved.getValue() : vm.target.getValue();
        if (item != null) {
            Glide.with(this).load(item.stickerUrl).into(b.stickerImage);
            b.nameText.setText(item.name);
        }
    }

    @Override public void onDestroyView() { super.onDestroyView(); b = null; }
}
