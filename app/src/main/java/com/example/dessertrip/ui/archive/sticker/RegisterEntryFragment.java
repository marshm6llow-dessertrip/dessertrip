package com.example.dessertrip.ui.archive.sticker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.dessertrip.R;
import com.example.dessertrip.databinding.FragmentRegisterEntryBinding;

/** 디저트 등록 — 사진 찍기 / 앨범에서 올리기 선택 */
public class RegisterEntryFragment extends Fragment {
    private FragmentRegisterEntryBinding b;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        b = FragmentRegisterEntryBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        final NavController nav = NavHostFragment.findNavController(this);
        b.topBar.titleText.setText("디저트 등록");
        b.topBar.closeButton.setVisibility(View.INVISIBLE);
        b.topBar.backButton.setOnClickListener(v -> { if (!nav.popBackStack()) requireActivity().finish(); });
        b.optionCamera.setOnClickListener(v -> nav.navigate(R.id.cameraFragment));
        b.optionGallery.setOnClickListener(v -> nav.navigate(R.id.galleryFragment));
    }

    @Override public void onDestroyView() { super.onDestroyView(); b = null; }
}
