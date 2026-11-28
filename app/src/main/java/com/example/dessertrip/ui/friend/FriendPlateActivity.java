package com.example.dessertrip.ui.friend;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.bumptech.glide.Glide;
import com.example.dessertrip.R;
import com.example.dessertrip.ui.collect.DessertApplication;
import com.example.dessertrip.ui.collect.core.network.ApiCallback;
import com.example.dessertrip.ui.collect.core.network.ApiError;
import com.example.dessertrip.ui.friend.data.PublicPlateDto;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

public class FriendPlateActivity extends AppCompatActivity {

    public static final String EXTRA_USER_ID = "user_id";

    private FrameLayout dessertPlate;

    private final List<PublicPlateDto.StickerItem> dessertStickers =
            new ArrayList<>();

    private View activeStickerLabel;
    private ImageView selectedStickerImage;

    private Call<PublicPlateDto> plateCall;

    private String friendNickname = "";
    private boolean plateLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_plate);

        dessertPlate = findViewById(R.id.layout_dessert_plate);

        dessertPlate.setOnClickListener(v ->
                removeStickerLabel());

        dessertPlate.addOnLayoutChangeListener(
                (v, l, t, r, b, oldL, oldT, oldR, oldB) -> {
                    if (r - l != oldR - oldL
                            || b - t != oldB - oldT) {
                        renderDessertStickers();
                    }
                }
        );

        int userId = getIntent().getIntExtra(EXTRA_USER_ID, -1);

        if (userId <= 0) {
            showToast("사용자 정보가 없습니다.");
            finish();
            return;
        }

        loadPublicPlate(userId);
    }

    // 1. 타인 공개 플레이트 조회
    private void loadPublicPlate(int userId) {

        TextView title = findViewById(R.id.tv_plate_title);
        title.setText("불러오는 중...");

        plateCall = DessertApplication.container(this)
                .friendRepository.getPublicPlate(
                        userId,
                        null,
                        new ApiCallback<PublicPlateDto>() {

                            @Override
                            public void onSuccess(PublicPlateDto data) {
                                if (isFinishing() || isDestroyed()) return;

                                bindPlateData(data);

                                dessertStickers.clear();

                                if (data.plate != null
                                        && data.plate.stickers != null) {
                                    dessertStickers.addAll(
                                            data.plate.stickers
                                    );
                                }

                                plateLoaded = true;

                                dessertPlate.post(
                                        FriendPlateActivity.this
                                                ::renderDessertStickers
                                );
                            }

                            @Override
                            public void onError(ApiError error) {
                                if (isFinishing() || isDestroyed()) return;

                                title.setText("플레이트 조회 실패");
                                showToast(error.userMessage);
                            }
                        }
                );
    }

    // 2. 서버에서 받은 스티커를 접시에 배치
    private void renderDessertStickers() {

        if (!plateLoaded || isFinishing() || isDestroyed()) return;

        removeStickerLabel();
        dessertPlate.removeAllViews();

        int plateWidth = dessertPlate.getWidth();
        int plateHeight = dessertPlate.getHeight();
        int count = dessertStickers.size();

        if (plateWidth <= 0 || plateHeight <= 0) {
            return;
        }

        // 빈 플레이트 처리
        if (count == 0) {

            TextView empty = new TextView(this);
            empty.setText("접시 위에 스티커가 없어요.");
            empty.setTextSize(13);
            empty.setTextColor(
                    ContextCompat.getColor(
                            this, R.color.text_secondary
                    )
            );

            dessertPlate.addView(
                    empty,
                    new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            Gravity.CENTER
                    )
            );

            return;
        }

        int diameter = Math.min(plateWidth, plateHeight);

        int imageSize = Math.max(1, (int) (
                diameter * Math.min(0.28, 0.95 / Math.sqrt(count))
        ));

        for (PublicPlateDto.StickerItem sticker : dessertStickers) {

            ImageView image = new ImageView(this);

            image.setContentDescription(sticker.stickerName);
            image.setScaleType(ImageView.ScaleType.FIT_CENTER);

            Glide.with(this)
                    .load(sticker.stickerImgUrl)
                    .into(image);

            FrameLayout.LayoutParams params =
                    new FrameLayout.LayoutParams(
                            imageSize, imageSize
                    );

            params.gravity = Gravity.TOP | Gravity.LEFT;

            // 서버의 정규화 좌표를 화면 좌표로 변환
            float x = (float) (sticker.positionX * plateWidth);
            float y = (float) (sticker.positionY * plateHeight);

            params.leftMargin = Math.max(
                    0,
                    Math.min(
                            Math.round(x - imageSize / 2f),
                            plateWidth - imageSize
                    )
            );

            params.topMargin = Math.max(
                    0,
                    Math.min(
                            Math.round(y - imageSize / 2f),
                            plateHeight - imageSize
                    )
            );

            dessertPlate.addView(image, params);

            image.setOnClickListener(v ->
                    showStickerLabel(image, sticker));
        }
    }

    // 3. 스티커 선택 말풍선
    private void showStickerLabel(
            ImageView image,
            PublicPlateDto.StickerItem sticker) {

        boolean sameSticker = selectedStickerImage == image;

        removeStickerLabel();

        if (sameSticker) {
            return;
        }

        selectedStickerImage = image;

        TextView label = new TextView(this);

        label.setTypeface(
                ResourcesCompat.getFont(this, R.font.pretendard)
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

        if (sticker.isGifted()) {

            label.setText("선물받은 스티커에요.");

            label.setTextColor(
                    ContextCompat.getColor(
                            this, R.color.text_secondary
                    )
            );

        } else if ("DIRECT".equals(sticker.acquisitionType)) {

            label.setText("요청하기");

            label.setTextColor(
                    ContextCompat.getColor(
                            this, R.color.text_primary
                    )
            );

            label.setTypeface(label.getTypeface(), Typeface.BOLD);

            label.setOnClickListener(v ->
                    showRequestDialog(sticker));

        } else {

            label.setText("요청할 수 없는 스티커에요.");
        }

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

    // 4. 말풍선 제거
    private void removeStickerLabel() {

        if (activeStickerLabel != null) {
            dessertPlate.removeView(activeStickerLabel);
            activeStickerLabel = null;
        }

        selectedStickerImage = null;
    }

    // 5. 스티커 요청 확인 팝업
    private void showRequestDialog(
            PublicPlateDto.StickerItem sticker) {

        new AlertDialog.Builder(this)
                .setTitle("스티커 요청")
                .setMessage(
                        friendNickname + "님에게 "
                                + sticker.stickerName
                                + " 스티커를 요청하시겠습니까?"
                )
                .setNegativeButton("취소", null)
                .setPositiveButton("요청하기", (dialog, which) -> {

                    // TODO: 스티커 요청 API 연동
                    showToast("스티커 요청 기능은 아직 연결되지 않았습니다.");

                    removeStickerLabel();
                })
                .show();
    }

    // 6. 프로필 및 수집 현황 표시
    private void bindPlateData(PublicPlateDto data) {

        TextView tvPlateTitle = findViewById(R.id.tv_plate_title);
        TextView tvFriendCode = findViewById(R.id.tv_friend_code);
        TextView tvFriendBio = findViewById(R.id.tv_friend_bio);

        TextView tvFriendLevel = findViewById(R.id.tv_friend_level);
        TextView tvNextLevel = findViewById(R.id.tv_next_level);
        TextView tvLevelCurrent = findViewById(R.id.tv_level_current);
        TextView tvLevelMax = findViewById(R.id.tv_level_max);

        TextView tvCakeCount = findViewById(R.id.tv_cake_count);
        TextView tvBreadCount = findViewById(R.id.tv_bread_count);
        TextView tvBakedCount = findViewById(R.id.tv_baked_count);
        TextView tvEtcCount = findViewById(R.id.tv_etc_count);
        TextView tvTotalCount = findViewById(R.id.tv_total_count);

        ProgressBar pbFriendLevel =
                findViewById(R.id.pb_friend_level);

        ImageView ivFriendProfile =
                findViewById(R.id.iv_friend_profile);

        friendNickname = data.userName;

        // 프로필 정보
        tvPlateTitle.setText(
                friendNickname + "의 디저트 플레이트"
        );

        tvFriendCode.setText(
                "친구 코드 : " + data.userFriendCode
        );

        tvFriendBio.setText(
                data.userDescription == null
                        ? ""
                        : data.userDescription
        );

        // 프로필 이미지
        Glide.with(this)
                .load(data.profileImageUrl)
                .placeholder(R.drawable.profile_sample)
                .fallback(R.drawable.profile_sample)
                .error(R.drawable.profile_sample)
                .into(ivFriendProfile);

        // 레벨 및 수집 현황
        int totalCount = data.totalStickerCount;
        int goal = data.nextLevelRequiredCount;

        tvFriendLevel.setText(
                "Lv. " + data.userLevel + " · " + data.userTitle
        );

        if (data.remainingStickerCount > 0) {

            tvNextLevel.setText(
                    "다음 레벨까지 "
                            + data.remainingStickerCount
                            + "개 디저트 기록"
            );

        } else {

            tvNextLevel.setText("목표 수집 개수 달성");
        }

        tvLevelCurrent.setText(String.valueOf(totalCount));
        tvLevelMax.setText("/ " + goal);

        pbFriendLevel.setMax(Math.max(1, goal));
        pbFriendLevel.setProgress(
                Math.min(totalCount, Math.max(1, goal))
        );

        // 카테고리별 수집 현황
        bindCategoryCount(tvCakeCount, data, "케이크");
        bindCategoryCount(tvBreadCount, data, "빵");
        bindCategoryCount(tvBakedCount, data, "구움과자");
        bindCategoryCount(tvEtcCount, data, "기타");

        tvTotalCount.setText(totalCount + "개 수집");
    }

    // 7. 카테고리별 수집 개수 표시
    private void bindCategoryCount(
            TextView view,
            PublicPlateDto data,
            String categoryName) {

        if (data.categoryCounts != null) {

            for (PublicPlateDto.CategoryCount category
                    : data.categoryCounts) {

                if (categoryName.equals(category.categoryName)) {

                    view.setVisibility(View.VISIBLE);

                    view.setText(
                            categoryName + " " + category.count
                    );

                    return;
                }
            }
        }

        // 서버에 없는 카테고리는 표시하지 않음
        view.setVisibility(View.GONE);
    }

    private int dp(float value) {
        return Math.round(
                value * getResources().getDisplayMetrics().density
        );
    }

    private void showToast(String message) {
        Toast.makeText(
                this,
                message,
                Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    protected void onDestroy() {

        if (plateCall != null) {
            plateCall.cancel();
        }

        super.onDestroy();
    }
}