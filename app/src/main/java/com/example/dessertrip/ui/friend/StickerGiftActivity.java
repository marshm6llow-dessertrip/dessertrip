package com.example.dessertrip.ui.friend;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.dessertrip.R;
import com.example.dessertrip.ui.collect.DessertApplication;
import com.example.dessertrip.ui.collect.core.network.ApiCallback;
import com.example.dessertrip.ui.collect.core.network.ApiError;
import com.example.dessertrip.ui.friend.data.FriendListDto;
import com.example.dessertrip.ui.friend.data.FriendRepository;
import com.example.dessertrip.ui.friend.data.StickerGiftCreateResponse;

import retrofit2.Call;

public class StickerGiftActivity extends AppCompatActivity {

    // 이전 화면에서 전달받을 스티커 정보
    public static final String EXTRA_STICKER_ID = "sticker_id";
    public static final String EXTRA_STICKER_NAME = "sticker_name";
    public static final String EXTRA_STICKER_IMAGE_URL = "sticker_img_url";
    public static final String EXTRA_DESSERT_STORE = "dessert_store";
    public static final String EXTRA_COLLECTION_NUMBER = "collection_number";

    private static final int MAX_MESSAGE_LENGTH = 200;
    private static final long SEARCH_DELAY_MS = 300;

    private EditText etFriendSearch;
    private EditText etGiftMessage;
    private TextView tvMessageCount;
    private TextView tvFriendStatus;
    private Button btnSendGift;
    private LinearLayout friendList;

    private FriendRepository friendRepository;

    private Call<FriendListDto> friendsCall;
    private Call<StickerGiftCreateResponse> giftCall;

    private final Handler searchHandler =
            new Handler(Looper.getMainLooper());

    private Runnable searchRunnable;

    private int searchVersion = 0;
    private int stickerId;

    private FriendListDto.FriendItem selectedFriend;
    private View selectedCard;
    private RadioButton selectedRadio;

    private boolean sending = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sticker_gift);

        etFriendSearch = findViewById(R.id.et_gift_friend_search);
        etGiftMessage = findViewById(R.id.et_gift_message);
        tvMessageCount = findViewById(R.id.tv_gift_message_count);
        tvFriendStatus = findViewById(R.id.tv_gift_friend_status);
        btnSendGift = findViewById(R.id.btn_send_sticker_gift);
        friendList = findViewById(R.id.layout_gift_friend_list);

        friendRepository = DessertApplication.container(this)
                .friendRepository;

        stickerId = getIntent().getIntExtra(EXTRA_STICKER_ID, -1);

        if (stickerId <= 0) {
            showToast("선물할 스티커 정보가 없습니다.");
            finish();
            return;
        }

        bindStickerData();

        findViewById(R.id.btn_gift_back)
                .setOnClickListener(v -> {
                    if (!sending) finish();
                });

        findViewById(R.id.btn_gift_close)
                .setOnClickListener(v -> {
                    if (!sending) finish();
                });

        btnSendGift.setText("스티커 선물 보내기");

        etGiftMessage.setText("");

        updateMessageCount();

        setupMessageListener();
        setupSearchListener();

        btnSendGift.setOnClickListener(v ->
                showGiftDialog());

        loadFriends("", ++searchVersion);
    }

    // 1. 선물할 스티커 정보 표시
    private void bindStickerData() {

        TextView tvCollectionNumber =
                findViewById(R.id.tv_gift_collection_number);

        TextView tvDessertName =
                findViewById(R.id.tv_gift_dessert_name);

        TextView tvDessertStore =
                findViewById(R.id.tv_gift_dessert_store);

        ImageView ivDessert =
                findViewById(R.id.iv_gift_dessert);

        String stickerName =
                getIntent().getStringExtra(EXTRA_STICKER_NAME);

        String imageUrl =
                getIntent().getStringExtra(EXTRA_STICKER_IMAGE_URL);

        String dessertStore =
                getIntent().getStringExtra(EXTRA_DESSERT_STORE);

        String collectionNumber =
                getIntent().getStringExtra(EXTRA_COLLECTION_NUMBER);

        tvDessertName.setText(
                stickerName == null
                        ? "선물할 스티커"
                        : stickerName
        );

        if (collectionNumber == null
                || collectionNumber.trim().isEmpty()) {

            tvCollectionNumber.setText("MY COLLECTION");

        } else {

            tvCollectionNumber.setText(
                    "MY COLLECTION · No. " + collectionNumber
            );
        }

        if (dessertStore == null
                || dessertStore.trim().isEmpty()) {

            tvDessertStore.setVisibility(View.GONE);

        } else {

            tvDessertStore.setText(dessertStore);
        }

        Glide.with(this)
                .load(imageUrl)
                .into(ivDessert);
    }

    // 2. 메시지 글자 수
    private void setupMessageListener() {

        etGiftMessage.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count) {

                updateMessageCount();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void updateMessageCount() {

        int count = etGiftMessage.getText().length();

        tvMessageCount.setText(
                count + " / " + MAX_MESSAGE_LENGTH
        );
    }

    // 3. 닉네임 검색
    private void setupSearchListener() {

        etFriendSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count) {

                String keyword = s.toString().trim();

                int version = ++searchVersion;

                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                if (friendsCall != null) {
                    friendsCall.cancel();
                }

                clearSelection();
                friendList.removeAllViews();

                showFriendStatus("검색 중...");

                searchRunnable = () ->
                        loadFriends(keyword, version);

                searchHandler.postDelayed(
                        searchRunnable,
                        SEARCH_DELAY_MS
                );
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    // 4. 서버 친구 목록 조회
    private void loadFriends(String keyword, int version) {

        showFriendStatus("친구 목록을 불러오는 중...");

        friendsCall = friendRepository.getFriends(
                keyword.isEmpty() ? null : keyword,
                new ApiCallback<FriendListDto>() {

                    @Override
                    public void onSuccess(FriendListDto data) {

                        if (isFinishing()
                                || isDestroyed()
                                || version != searchVersion) {
                            return;
                        }

                        clearSelection();
                        friendList.removeAllViews();

                        if (data == null
                                || data.friends == null
                                || data.friends.isEmpty()) {

                            showFriendStatus(
                                    keyword.isEmpty()
                                            ? "등록된 친구가 없어요."
                                            : "검색 결과가 없어요."
                            );

                            return;
                        }

                        tvFriendStatus.setVisibility(View.GONE);

                        for (FriendListDto.FriendItem friend
                                : data.friends) {

                            addFriendCard(friend);
                        }
                    }

                    @Override
                    public void onError(ApiError error) {

                        if (isFinishing()
                                || isDestroyed()
                                || version != searchVersion) {
                            return;
                        }

                        showFriendStatus("친구 목록 조회 실패");
                        showToast(error.userMessage);
                    }
                }
        );
    }

    // 5. 친구 카드 생성
    private void addFriendCard(
            FriendListDto.FriendItem friend) {

        View card = getLayoutInflater().inflate(
                R.layout.item_gift_friend,
                friendList,
                false
        );

        ImageView avatar =
                card.findViewById(R.id.iv_gift_friend_avatar);

        TextView name =
                card.findViewById(R.id.tv_gift_friend_name);

        TextView info =
                card.findViewById(R.id.tv_gift_friend_info);

        RadioButton radio =
                card.findViewById(R.id.rb_gift_friend);

        name.setText(friend.nickname);

        String friendCode = friend.friendCode == null
                ? ""
                : friend.friendCode;

        String userTitle = friend.userTitle == null
                ? ""
                : friend.userTitle;

        info.setText(
                friendCode
                        + " · Lv. "
                        + friend.userLevel
                        + " "
                        + userTitle
        );

        Glide.with(this)
                .load(friend.profileImageUrl)
                .placeholder(R.drawable.profile_sample)
                .fallback(R.drawable.profile_sample)
                .error(R.drawable.profile_sample)
                .into(avatar);

        card.setOnClickListener(v ->
                selectFriend(friend, card, radio));

        radio.setOnClickListener(v ->
                selectFriend(friend, card, radio));

        friendList.addView(card);
    }

    // 6. 친구 한 명만 선택
    private void selectFriend(
            FriendListDto.FriendItem friend,
            View card,
            RadioButton radio) {

        if (sending) {

            radio.setChecked(selectedRadio == radio);
            return;
        }

        boolean sameFriend = selectedFriend != null
                && selectedFriend.userId == friend.userId;

        clearSelection();

        if (sameFriend) {
            radio.setChecked(false);
            return;
        }

        selectedFriend = friend;
        selectedCard = card;
        selectedRadio = radio;

        radio.setChecked(true);

        card.setBackgroundResource(
                R.drawable.bg_gift_friend_selected
        );
    }

    private void clearSelection() {

        if (selectedRadio != null) {
            selectedRadio.setChecked(false);
        }

        if (selectedCard != null) {

            selectedCard.setBackgroundResource(
                    R.drawable.bg_gift_friend_unselected
            );
        }

        selectedFriend = null;
        selectedCard = null;
        selectedRadio = null;
    }

    // 7. 선물 전송 확인 팝업
    private void showGiftDialog() {

        if (sending) return;

        if (selectedFriend == null) {

            showToast("선물받을 친구를 선택해 주세요.");
            return;
        }

        String message = etGiftMessage.getText().toString();

        if (message.length() > MAX_MESSAGE_LENGTH) {

            showToast("메시지는 200자 이하로 입력해 주세요.");
            return;
        }

        FriendListDto.FriendItem receiver = selectedFriend;

        new AlertDialog.Builder(this)
                .setTitle("스티커 선물하기")
                .setMessage(
                        receiver.nickname
                                + "님에게 스티커 선물을 보낼까요?"
                )
                .setNegativeButton("취소", null)
                .setPositiveButton("보내기", (dialog, which) ->
                        sendGift(receiver.userId, message))
                .show();
    }

    // 8. 실제 선물 전송 API
    private void sendGift(int receiverId, String message) {

        if (sending) return;

        setSending(true);

        giftCall = friendRepository.sendStickerGift(
                stickerId,
                receiverId,
                message,
                new ApiCallback<StickerGiftCreateResponse>() {

                    @Override
                    public void onSuccess(
                            StickerGiftCreateResponse data) {

                        if (isFinishing() || isDestroyed()) return;

                        showToast("스티커 선물을 보냈어요.");

                        setResult(RESULT_OK);
                        finish();
                    }

                    @Override
                    public void onError(ApiError error) {

                        if (isFinishing() || isDestroyed()) return;

                        setSending(false);
                        showToast(error.userMessage);
                    }
                }
        );
    }

    private void setSending(boolean value) {

        sending = value;

        btnSendGift.setEnabled(!value);
        etFriendSearch.setEnabled(!value);
        etGiftMessage.setEnabled(!value);

        btnSendGift.setText(
                value
                        ? "전송 중..."
                        : "스티커 선물 보내기"
        );

        for (int i = 0; i < friendList.getChildCount(); i++) {

            View card = friendList.getChildAt(i);

            card.setEnabled(!value);

            RadioButton radio =
                    card.findViewById(R.id.rb_gift_friend);

            radio.setEnabled(!value);
        }
    }

    private void showFriendStatus(String message) {

        tvFriendStatus.setText(message);
        tvFriendStatus.setVisibility(View.VISIBLE);
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

        searchHandler.removeCallbacksAndMessages(null);

        if (friendsCall != null) {
            friendsCall.cancel();
        }

        if (giftCall != null) {
            giftCall.cancel();
        }

        super.onDestroy();
    }
}