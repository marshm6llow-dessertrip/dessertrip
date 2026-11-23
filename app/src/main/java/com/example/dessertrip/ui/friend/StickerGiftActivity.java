package com.example.dessertrip.ui.friend;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dessertrip.R;

public class StickerGiftActivity extends AppCompatActivity {

    // TODO: 실제 API 연동 시 테스트 데이터 교체
    private static final String TEST_DESSERT_NAME = "딸기 구름 케이크";
    private static final String TEST_DESSERT_STORE = "오후의 조각 · 연남동";
    private static final String TEST_COLLECTION_NUMBER = "024";

    private static final String TEST_FRIEND_NAME = "skrr";

    private static final String TEST_FRIEND_AVATAR = "다은";

    private static final String TEST_GIFT_MESSAGE =
            "다은아, 네가 좋아할 것 같아서 골랐어 🍓\n"
                    + "다음엔 우리 같이 먹으러 가자!\n"
                    + "오늘도 달콤한 하루 보내 ♡";

    private static final int MAX_MESSAGE_LENGTH = 200;

    private EditText etFriendSearch;
    private EditText etGiftMessage;
    private TextView tvMessageCount;
    private RadioButton rbGiftFriend;
    private Button btnSendGift;
    private View friendCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sticker_gift);

        etFriendSearch = findViewById(R.id.et_gift_friend_search);
        etGiftMessage = findViewById(R.id.et_gift_message);
        tvMessageCount = findViewById(R.id.tv_gift_message_count);
        rbGiftFriend = findViewById(R.id.rb_gift_friend);
        btnSendGift = findViewById(R.id.btn_send_sticker_gift);
        friendCard = findViewById(R.id.layout_gift_friend);

        bindTestData();

        // 친구 선택 시 카드 배경 변경
        rbGiftFriend.setOnCheckedChangeListener((button, isChecked) -> {
            friendCard.setBackgroundResource(
                    isChecked
                            ? R.drawable.bg_gift_friend_selected
                            : R.drawable.bg_gift_friend_unselected
            );
        });

    // 카드의 빈 공간 클릭 시 선택 및 해제
        friendCard.setOnClickListener(v -> {
            if (rbGiftFriend.isEnabled()) {
                rbGiftFriend.setChecked(!rbGiftFriend.isChecked());
            }
        });

// RadioButton 자체 클릭은 기본 동작 사용
// 별도의 setOnClickListener를 추가하지 않음
        // 뒤로가기 및 닫기
        findViewById(R.id.btn_gift_back)
                .setOnClickListener(v -> finish());

        findViewById(R.id.btn_gift_close)
                .setOnClickListener(v -> finish());

        // 메시지 글자 수
        updateMessageCount();

        etGiftMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {
                updateMessageCount();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // 임시 친구 검색
        etFriendSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {

                String keyword = s.toString().trim();

                boolean matches = keyword.isEmpty()
                        || TEST_FRIEND_NAME.contains(keyword);

                if (!matches) {
                    rbGiftFriend.setChecked(false);
                }

                rbGiftFriend.setEnabled(matches);
                friendCard.setVisibility(
                        matches ? View.VISIBLE : View.GONE
                );
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // 테스트용 선물 보내기
        btnSendGift.setOnClickListener(v -> {

            if (!rbGiftFriend.isChecked()) {
                Toast.makeText(
                        this,
                        "선물받을 친구를 선택해 주세요.",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            new AlertDialog.Builder(this)
                    .setTitle("스티커 선물하기")
                    .setMessage(
                            TEST_FRIEND_NAME + "님에게 스티커 선물을 보낼까요?"
                    )
                    .setNegativeButton("취소", (dialog, which) -> dialog.dismiss())
                    .setPositiveButton("보내기", (dialog, which) -> {
                        // TODO: 실제 API 연동 시 선물 전송 요청
                        Toast.makeText(
                                this,
                                "테스트용 선물입니다. 실제로 전송되지 않았습니다.",
                                Toast.LENGTH_LONG
                        ).show();
                    })
                    .show();
        });
    }

    private void bindTestData() {
        TextView tvCollectionNumber =
                findViewById(R.id.tv_gift_collection_number);
        TextView tvDessertName =
                findViewById(R.id.tv_gift_dessert_name);
        TextView tvDessertStore =
                findViewById(R.id.tv_gift_dessert_store);
        TextView tvFriendName =
                findViewById(R.id.tv_gift_friend_name);
                TextView tvFriendAvatar =
                findViewById(R.id.tv_gift_friend_avatar);

        tvCollectionNumber.setText(
                "MY COLLECTION · No. " + TEST_COLLECTION_NUMBER
        );

        tvDessertName.setText(TEST_DESSERT_NAME);
        tvDessertStore.setText(TEST_DESSERT_STORE);

        tvFriendName.setText(TEST_FRIEND_NAME);

        tvFriendAvatar.setText(TEST_FRIEND_AVATAR);
        etGiftMessage.setText(TEST_GIFT_MESSAGE);

        btnSendGift.setText("스티커 선물 보내기");
    }

    private void updateMessageCount() {
        int count = etGiftMessage.getText().length();

        tvMessageCount.setText(
                count + " / " + MAX_MESSAGE_LENGTH
        );
    }
}
