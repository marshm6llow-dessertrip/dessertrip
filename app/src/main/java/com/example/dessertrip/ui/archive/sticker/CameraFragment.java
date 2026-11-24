package com.example.dessertrip.ui.archive.sticker;

import android.content.res.ColorStateList;
import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.core.ZoomState;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.common.util.concurrent.ListenableFuture;

import com.example.dessertrip.R;
import com.example.dessertrip.databinding.FragmentCameraBinding;
import com.example.dessertrip.ui.collect.common.ViewModels;

import java.io.File;
import java.util.concurrent.ExecutionException;

/** CameraX 촬영 화면. 촬영 결과는 RegisterFlowViewModel 에 담고 스티커 화면으로 이동한다. */
public class CameraFragment extends Fragment {
    private FragmentCameraBinding b;
    private ProcessCameraProvider provider;
    private ImageCapture imageCapture;
    private Camera camera;
    private int lens = CameraSelector.LENS_FACING_BACK;
    private boolean flashOn = false;
    private float zoom = 1f;

    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) startCamera();
                else if (b != null) b.permissionText.setVisibility(View.VISIBLE);
            });

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        b = FragmentCameraBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        final NavController nav = NavHostFragment.findNavController(this);
        b.closeButton.setOnClickListener(v -> nav.popBackStack());
        b.galleryButton.setOnClickListener(v -> nav.navigate(R.id.galleryFragment));
        b.permissionText.setOnClickListener(v -> permissionLauncher.launch(Manifest.permission.CAMERA));
        b.flashButton.setOnClickListener(v -> {
            flashOn = !flashOn;
            b.flashButton.setImageResource(flashOn ? R.drawable.ic_flash_on : R.drawable.ic_flash_off);
            if (imageCapture != null) {
                imageCapture.setFlashMode(flashOn ? ImageCapture.FLASH_MODE_ON : ImageCapture.FLASH_MODE_OFF);
            }
        });
        b.flipButton.setOnClickListener(v -> {
            lens = lens == CameraSelector.LENS_FACING_BACK ? CameraSelector.LENS_FACING_FRONT : CameraSelector.LENS_FACING_BACK;
            bindCamera();
        });
        b.zoomHalf.setOnClickListener(v -> setZoom(0.5f));
        b.zoomOne.setOnClickListener(v -> setZoom(1f));
        b.shutterButton.setOnClickListener(v -> takePhoto());
        styleZoomPills();
        startCamera();
    }

    private boolean hasPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void startCamera() {
        if (b == null) return;
        if (!hasPermission()) {
            permissionLauncher.launch(Manifest.permission.CAMERA);
            return;
        }
        b.permissionText.setVisibility(View.GONE);
        final ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(requireContext());
        future.addListener(() -> {
            try {
                provider = future.get();
                bindCamera();
            } catch (ExecutionException | InterruptedException e) {
                if (getContext() != null) Toast.makeText(getContext(), "카메라를 시작할 수 없어요.", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void bindCamera() {
        if (provider == null || b == null || !isAdded()) return;
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(b.previewView.getSurfaceProvider());
        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setFlashMode(flashOn ? ImageCapture.FLASH_MODE_ON : ImageCapture.FLASH_MODE_OFF)
                .build();
        provider.unbindAll();
        try {
            camera = provider.bindToLifecycle(getViewLifecycleOwner(),
                    new CameraSelector.Builder().requireLensFacing(lens).build(), preview, imageCapture);
            zoom = 1f;
            styleZoomPills();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "이 카메라를 사용할 수 없어요.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setZoom(float ratio) {
        if (camera == null) return;
        ZoomState state = camera.getCameraInfo().getZoomState().getValue();
        float clamped = ratio;
        if (state != null) clamped = Math.max(state.getMinZoomRatio(), Math.min(state.getMaxZoomRatio(), ratio));
        camera.getCameraControl().setZoomRatio(clamped);
        zoom = ratio;
        styleZoomPills();
    }

    private void styleZoomPills() {
        stylePill(b.zoomHalf, zoom == 0.5f);
        stylePill(b.zoomOne, zoom == 1f);
    }

    private void stylePill(TextView v, boolean on) {
        v.setBackgroundResource(R.drawable.bg_round_100);
        v.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(v.getContext(), on ? R.color.white : R.color.scrim)));
        v.setTextColor(ContextCompat.getColor(requireContext(), on ? R.color.black : R.color.white));
    }

    private void takePhoto() {
        if (imageCapture == null) return;
        final File file = new File(requireContext().getCacheDir(), "capture_" + System.currentTimeMillis() + ".jpg");
        imageCapture.takePicture(new ImageCapture.OutputFileOptions.Builder(file).build(),
                ContextCompat.getMainExecutor(requireContext()),
                new ImageCapture.OnImageSavedCallback() {
                    @Override public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                        if (!isAdded()) return;
                        ViewModels.register(CameraFragment.this).setPicked(Uri.fromFile(file));
                        NavHostFragment.findNavController(CameraFragment.this).navigate(R.id.stickerFragment);
                    }

                    @Override public void onError(@NonNull ImageCaptureException exception) {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "사진을 저장하지 못했어요. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        if (provider != null) provider.unbindAll();
        camera = null;
        imageCapture = null;
        b = null;
    }
}
