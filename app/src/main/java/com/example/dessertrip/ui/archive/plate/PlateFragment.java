package com.example.dessertrip.ui.archive.plate;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import com.example.dessertrip.R;
import com.example.dessertrip.databinding.FragmentPlateBinding;
import com.example.dessertrip.ui.archive.collect.ArchiveHeader;
import com.example.dessertrip.ui.collect.common.ViewModels;
import com.example.dessertrip.ui.collect.core.network.ApiError;
import com.example.dessertrip.ui.collect.data.PlateDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 플레이트 화면. 팀의 FriendPlateActivity 를 뼈대로 만들었다.
 *  - 팀 코드: 프로필·레벨 표시(bindPlateData), 접시 위 무작위 배치(renderDessertStickers), 스티커 말풍선(showStickerLabel)
 *  - 우리 코드: 서버 데이터(PlateViewModel), 스티커 선택 대화상자(PlatePicker), 말풍선을 "삭제하기"로 사용,
 *    디저트 종류별 보기(칩), 많이 기록한 카테고리 3개 정렬(ArchiveHeader.topCategoryLabels)
 */
public class PlateFragment extends Fragment {
    private FragmentPlateBinding b;
    private PlateViewModel vm;
    private AlertDialog pickerDialog;

    private FrameLayout dessertPlate;
    private final Random random = new Random();
    private View activeStickerLabel;
    private ImageView selectedStickerImage;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        b = FragmentPlateBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        vm = ViewModels.of(this, PlateViewModel.class);
        dessertPlate = b.layoutDessertPlate;

        b.backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        b.retryButton.setOnClickListener(v -> vm.load());
        b.addStickerButton.setOnClickListener(v -> pickerDialog = PlatePicker.show(this, vm));
        dessertPlate.setOnClickListener(v -> removeStickerLabel());
        // 접시 크기가 정해지거나 바뀌면(처음 보일 때, 화면 회전) 그 크기에 맞춰 다시 배치한다
        dessertPlate.addOnLayoutChangeListener((v, l, t, r, bt, oldL, oldT, oldR, oldB) -> {
            if (r - l != oldR - oldL || bt - t != oldB - oldT) renderDessertStickers();
        });

        vm.loading.observe(getViewLifecycleOwner(), l -> updateState());
        vm.error.observe(getViewLifecycleOwner(), e -> updateState());
        vm.plate.observe(getViewLifecycleOwner(), this::bindPlateData);
        vm.placed.observe(getViewLifecycleOwner(), list -> { renderChips(); dessertPlate.post(this::renderDessertStickers); });
        vm.category.observe(getViewLifecycleOwner(), c -> { renderChips(); dessertPlate.post(this::renderDessertStickers); });
        ViewModels.showMessages(this, vm.message);
    }

    private void updateState() {
        boolean loading = Boolean.TRUE.equals(vm.loading.getValue());
        ApiError error = vm.error.getValue();
        b.loadingSpinner.setVisibility(loading ? View.VISIBLE : View.GONE);
        b.errorLayout.setVisibility(!loading && error != null ? View.VISIBLE : View.GONE);
        if (error != null) b.errorText.setText(error.userMessage);
        b.scroll.setVisibility(!loading && error == null ? View.VISIBLE : View.GONE);
    }

    // 플레이트 데이터 화면에 적용 (팀의 bindPlateData 에서 테스트 데이터 대신 서버 데이터를 쓴다)
    private void bindPlateData(PlateDto plate) {
        if (plate == null) return;
        int collected = plate.collectedCount;
        int goal = Math.max(1, plate.goalCount);

        // 프로필 데이터
        b.tvPlateTitle.setText(plate.nickname + "의 디저트 플레이트");
        boolean hasCode = plate.friendCode != null && !plate.friendCode.isEmpty();
        b.tvFriendCode.setVisibility(hasCode ? View.VISIBLE : View.GONE);
        if (hasCode) b.tvFriendCode.setText("친구 코드 : " + plate.friendCode);
        boolean hasBio = plate.bio != null && !plate.bio.isEmpty();
        b.tvFriendBio.setVisibility(hasBio ? View.VISIBLE : View.GONE);
        if (hasBio) b.tvFriendBio.setText(plate.bio);

        // 레벨 데이터
        b.tvFriendLevel.setText("Lv. " + plate.level + " · " + plate.levelTitle);
        b.tvNextLevel.setText("다음 레벨까지 " + Math.max(0, goal - collected) + "개 디저트 기록");
        b.tvLevelCurrent.setText(String.valueOf(collected));
        b.tvLevelMax.setText("/ " + goal);
        b.pbFriendLevel.setMax(goal);
        b.pbFriendLevel.setProgress(Math.min(collected, goal));

        // 가장 많이 기록한 카테고리 3개 (많은 순서대로 왼쪽부터)
        String[] top = ArchiveHeader.topCategoryLabels(plate);
        b.tvTopCategory1.setText(top[0]);
        b.tvTopCategory2.setText(top[1]);
        b.tvTopCategory3.setText(top[2]);

        // 전체 수집 개수
        b.tvTotalCount.setText(collected + "개 수집");
    }

    /** (우리 코드) 칩: 접시 위에 놓인 스티커를 카테고리별로 센다. 누르면 그 종류만 접시에 보인다. */
    private void renderChips() {
        List<PlateViewModel.Placed> all = vm.placed.getValue();
        if (all == null) all = new ArrayList<>();
        List<String> categories = new ArrayList<>();
        for (PlateViewModel.Placed p : all) categories.add(p.category);
        Map<String, Integer> counts = ArchiveHeader.countByCategory(categories);
        String selected = vm.category.getValue();
        if (selected != null && !counts.containsKey(selected)) {   // 고른 종류의 스티커가 모두 빠졌으면 "전체"로 돌아간다
            vm.selectCategory(null);
            return;
        }
        ArchiveHeader.bindChips(b.chips, getLayoutInflater(), selected, counts, all.size(), vm::selectCategory);
    }

    // 접시 위에 스티커를 무작위로 겹치지 않게 배치 (팀 코드 그대로, 이미지만 서버 주소(Glide)로 불러온다)
    private void renderDessertStickers() {
        if (b == null) return;

        dessertPlate.removeAllViews();
        activeStickerLabel = null;
        selectedStickerImage = null;

        List<PlateViewModel.Placed> stickers = new ArrayList<>();
        String selected = vm.category.getValue();
        List<PlateViewModel.Placed> all = vm.placed.getValue();
        if (all != null) for (PlateViewModel.Placed p : all) if (selected == null || selected.equals(p.category)) stickers.add(p);

        int plateWidth = dessertPlate.getWidth();
        int plateHeight = dessertPlate.getHeight();
        int count = stickers.size();

        if (plateWidth <= 0 || plateHeight <= 0) {
            return;
        }
        if (count == 0) {   // (우리 코드) 비어 있을 때 안내 문구
            TextView empty = new TextView(requireContext());
            empty.setText(selected == null ? "접시 위에 스티커를 올려보세요" : "이 종류의 스티커가 접시에 없어요");
            empty.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
            empty.setTextSize(13);
            dessertPlate.addView(empty, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
            return;
        }

        int diameter = Math.min(plateWidth, plateHeight);

        float centerX = plateWidth / 2f;
        float centerY = plateHeight / 2f;

        float radius = diameter * 0.56f;

        int imageSize = Math.max(1, (int) (
                diameter * Math.min(0.28, 0.95 / Math.sqrt(count))
        ));

        List<float[]> positions = new ArrayList<>();

        // 모든 스티커의 위치를 찾을 때까지 재시도
        while (imageSize > 0) {

            positions.clear();

            float availableRadius =
                    radius - imageSize * 0.71f;

            float minDistance = imageSize * 0.8f;

            for (int i = 0; i < count; i++) {

                for (int attempt = 0; attempt < 500; attempt++) {

                    float x = centerX
                            + (random.nextFloat() * 2 - 1)
                            * availableRadius;

                    float y = centerY
                            + (random.nextFloat() * 2 - 1)
                            * availableRadius;

                    // 접시 내부인지 확인
                    if (Math.hypot(x - centerX, y - centerY)
                            > availableRadius) {
                        continue;
                    }

                    boolean overlapping = false;

                    for (float[] position : positions) {

                        double distance = Math.hypot(
                                x - position[0],
                                y - position[1]
                        );

                        if (distance < minDistance) {
                            overlapping = true;
                            break;
                        }
                    }

                    if (!overlapping) {
                        positions.add(new float[]{x, y});
                        break;
                    }
                }
            }

            // 모든 스티커 배치 성공
            if (positions.size() == count) {
                break;
            }

            // 실패하면 크기를 줄이고 처음부터 다시 배치
            imageSize = Math.min(imageSize - 1,
                    (int) (imageSize * 0.95f));
        }

        if (imageSize <= 0 || positions.size() != count) {
            return;
        }

        // 위치가 모두 결정된 후 이미지 생성
        for (int i = 0; i < count; i++) {

            PlateViewModel.Placed sticker = stickers.get(i);

            ImageView image = new ImageView(requireContext());

            Glide.with(this).load(sticker.imageUrl).into(image);
            image.setContentDescription(sticker.name);
            image.setScaleType(ImageView.ScaleType.FIT_CENTER);

            FrameLayout.LayoutParams params =
                    new FrameLayout.LayoutParams(
                            imageSize, imageSize
                    );

            params.gravity = Gravity.TOP | Gravity.LEFT;

            params.leftMargin = (int) (
                    positions.get(i)[0] - imageSize / 2f
            );

            params.topMargin = (int) (
                    positions.get(i)[1] - imageSize / 2f
            );

            dessertPlate.addView(image, params);
            image.setOnClickListener(v ->
                    showStickerLabel(image, sticker));
        }
    }

    // 스티커를 누르면 말풍선이 뜬다 (팀 코드의 말풍선. 팀은 "요청하기", 우리는 "삭제하기")
    private void showStickerLabel(
            ImageView image,
            PlateViewModel.Placed sticker) {

        boolean sameSticker = selectedStickerImage == image;

        removeStickerLabel();

        // 같은 스티커를 다시 누르면 닫기
        if (sameSticker) {
            return;
        }

        selectedStickerImage = image;

        TextView label = new TextView(requireContext());

        // 공통 폰트 적용
        label.setTypeface(
                ResourcesCompat.getFont(requireContext(), R.font.pretendard)
        );

        label.setGravity(Gravity.CENTER);

        int verticalPadding = getResources()
                .getDimensionPixelSize(R.dimen.space_s);

        label.setPadding(
                dp(10),
                verticalPadding,
                dp(10),
                verticalPadding
        );

        label.setBackgroundResource(R.drawable.bg_friend_input);
        label.setTextSize(14);

        if (sticker.gift) {

            label.setText("선물받은 스티커 · 삭제하기");

            label.setTextColor(
                    ContextCompat.getColor(
                            requireContext(), R.color.text_secondary
                    )
            );

        } else {

            label.setText("삭제하기");

            label.setTextColor(
                    ContextCompat.getColor(
                            requireContext(), R.color.text_primary
                    )
            );

            label.setTypeface(label.getTypeface(), Typeface.BOLD);
        }

        // 접시에서만 빼고, 도감의 스티커는 그대로 둔다
        label.setOnClickListener(v -> {
            removeStickerLabel();
            vm.remove(sticker.stickerId);
        });

        FrameLayout.LayoutParams params =
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                );

        label.setVisibility(View.INVISIBLE);
        dessertPlate.addView(label, params);

        activeStickerLabel = label;

        label.post(() -> {

            if (activeStickerLabel != label) {
                return;
            }

            float x = image.getX()
                    + (image.getWidth() - label.getWidth()) / 2f;

            float y = image.getY() - label.getHeight() / 2f;

            x = Math.max(0, Math.min(
                    x, dessertPlate.getWidth() - label.getWidth()));

            y = Math.max(0, Math.min(
                    y, dessertPlate.getHeight() - label.getHeight()));

            label.setX(x);
            label.setY(y);
            label.bringToFront();
            label.setVisibility(View.VISIBLE);
        });
    }

    private void removeStickerLabel() {

        if (activeStickerLabel != null) {
            dessertPlate.removeView(activeStickerLabel);
            activeStickerLabel = null;
        }

        selectedStickerImage = null;
    }

    private int dp(float value) {
        return Math.round(
                value * getResources().getDisplayMetrics().density
        );
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        if (pickerDialog != null) { pickerDialog.dismiss(); pickerDialog = null; }
        activeStickerLabel = null;
        selectedStickerImage = null;
        dessertPlate = null;
        b = null;
    }
}
