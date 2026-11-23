package com.example.dessertrip.ui.friend;

import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dessertrip.R;

import java.util.ArrayList;
import java.util.List;
import android.widget.ProgressBar;
import java.util.Random;
import android.view.View;
import android.view.Gravity;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

public class FriendPlateActivity extends AppCompatActivity {

    private FrameLayout dessertPlate;
    private final List<DessertSticker> dessertStickers = new ArrayList<>();
    private final Random random = new Random();

    private View activeStickerLabel;
    private ImageView selectedStickerImage;

    //TODO: 친구 플레이트 API 연동 시 테스트용 데이터 제거
    private static final String TEST_FRIEND_NICKNAME = "딸기시루";
    private static final String TEST_FRIEND_CODE = "#41237802";
    private static final String TEST_FRIEND_BIO = "딸기 시루 나에게로. 🍓";

    // 테스트용 레벨 데이터
    private static final int TEST_FRIEND_LEVEL = 3;
    private static final String TEST_LEVEL_TITLE = "초보 탐험가";
    private static final int TEST_NEXT_LEVEL_TARGET = 30;

    // 테스트용 카테고리별 수집 데이터
    private static final int TEST_CAKE_COUNT = 8;
    private static final int TEST_BREAD_COUNT = 7;
    private static final int TEST_BAKED_COUNT = 2;
    private static final int TEST_ETC_COUNT = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_plate);

        // 친구 플레이트 데이터 적용
        bindPlateData();

        dessertPlate = findViewById(R.id.layout_dessert_plate);
        dessertPlate.setOnClickListener(v ->
                removeStickerLabel());


        loadSampleStickers();
        // 접시의 크기가 결정된 후 랜덤 배치
        dessertPlate.post(this::renderDessertStickers);
    }

    //TODO: 친구 스티커 목록 API 연동
    private void loadSampleStickers() {

        dessertStickers.clear();

        dessertStickers.add(new DessertSticker(
                R.drawable.dessert_donut, "도넛", true));

        dessertStickers.add(new DessertSticker(
                R.drawable.dessert_croissant, "크루아상", false));

        dessertStickers.add(new DessertSticker(
                R.drawable.dessert_canele, "까눌레", false));

        dessertStickers.add(new DessertSticker(
                R.drawable.dessert_cake, "케이크", false));

        dessertStickers.add(new DessertSticker(
                R.drawable.dessert_choux, "슈", false));
    }

    private void renderDessertStickers() {

        dessertPlate.removeAllViews();

        int plateWidth = dessertPlate.getWidth();
        int plateHeight = dessertPlate.getHeight();
        int count = dessertStickers.size();

        if (plateWidth <= 0 || plateHeight <= 0 || count == 0) {
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

            DessertSticker sticker = dessertStickers.get(i);

            ImageView image = new ImageView(this);

            image.setImageResource(sticker.getImageResId());
            image.setContentDescription(sticker.getName());
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

    private void showStickerLabel(
            ImageView image,
            DessertSticker sticker) {

        boolean sameSticker = selectedStickerImage == image;

        removeStickerLabel();

        // 같은 스티커를 다시 누르면 닫기
        if (sameSticker) {
            return;
        }

        selectedStickerImage = image;

        TextView label = new TextView(this);

        // 공통 폰트 적용
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

        } else {

            label.setText("요청하기");

            label.setTextColor(
                    ContextCompat.getColor(
                            this, R.color.text_primary
                    )
            );

            label.setTypeface(label.getTypeface(), Typeface.BOLD);

            label.setOnClickListener(v ->
                    showRequestDialog(sticker));
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

    private void removeStickerLabel() {

        if (activeStickerLabel != null) {
            dessertPlate.removeView(activeStickerLabel);
            activeStickerLabel = null;
        }

        selectedStickerImage = null;
    }

    private void showRequestDialog(DessertSticker sticker) {

        new AlertDialog.Builder(this)
                .setTitle("스티커 요청")
                .setMessage(
                        TEST_FRIEND_NICKNAME + "님에게 "
                                + sticker.getName()
                                + " 스티커를 요청하시겠습니까?"
                )
                .setNegativeButton("취소", null)
                .setPositiveButton("요청하기", (dialog, which) -> {

                    // TODO: 스티커 요청 API 연동
                    Toast.makeText(
                            this,
                            "테스트용 요청입니다.",
                            Toast.LENGTH_SHORT
                    ).show();

                    removeStickerLabel();
                })
                .show();
    }

    private int dp(float value) {
        return Math.round(
                value * getResources().getDisplayMetrics().density
        );
    }
    // 친구 플레이트 데이터 화면에 적용
    private void bindPlateData() {

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

        ProgressBar pbFriendLevel = findViewById(R.id.pb_friend_level);

        // 전체 수집 개수 계산
        int totalCount = TEST_CAKE_COUNT
                + TEST_BREAD_COUNT
                + TEST_BAKED_COUNT
                + TEST_ETC_COUNT;

        // 다음 레벨까지 남은 개수
        int remainingCount = Math.max(
                0, TEST_NEXT_LEVEL_TARGET - totalCount
        );

        // 프로필 데이터
        tvPlateTitle.setText(
                TEST_FRIEND_NICKNAME + "의 디저트 플레이트"
        );
        tvFriendCode.setText("친구 코드 : " + TEST_FRIEND_CODE);
        tvFriendBio.setText(TEST_FRIEND_BIO);

        // 레벨 데이터
        tvFriendLevel.setText(
                "Lv. " + TEST_FRIEND_LEVEL + " · " + TEST_LEVEL_TITLE
        );
        tvNextLevel.setText(
                "다음 레벨까지 " + remainingCount + "개 디저트 기록"
        );
        tvLevelCurrent.setText(String.valueOf(totalCount));
        tvLevelMax.setText("/ " + TEST_NEXT_LEVEL_TARGET);

        // 레벨 진행률
        pbFriendLevel.setMax(TEST_NEXT_LEVEL_TARGET);
        pbFriendLevel.setProgress(totalCount);

        // 카테고리별 수집 개수
        tvCakeCount.setText("케이크 " + TEST_CAKE_COUNT);
        tvBreadCount.setText("빵 " + TEST_BREAD_COUNT);
        tvBakedCount.setText("구움과자 " + TEST_BAKED_COUNT);
        tvEtcCount.setText("기타 " + TEST_ETC_COUNT);

        // 전체 수집 개수
        tvTotalCount.setText(totalCount + "개 수집");
    }

}
