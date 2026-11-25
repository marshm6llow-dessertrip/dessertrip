package com.example.dessertrip.ui.friend;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dessertrip.R;
import com.example.dessertrip.ui.collect.AppContainer;
import com.example.dessertrip.ui.collect.DessertApplication;
import com.example.dessertrip.ui.collect.core.network.ApiCallback;
import com.example.dessertrip.ui.collect.core.network.ApiError;
import com.example.dessertrip.ui.collect.data.ProfileDto;
import com.example.dessertrip.ui.friend.data.FriendCodeSearchDto;
import com.example.dessertrip.ui.friend.data.FriendRepository;
import com.example.dessertrip.ui.friend.data.FriendRequestResponseDto;

import retrofit2.Call;

public class FriendAddActivity extends AppCompatActivity {

    private TextView tvMyFriendCode;
    private TextView tvFriendName;
    private TextView tvFriendCode;
    private TextView tvFriendBio;

    private EditText etFriendCode;
    private LinearLayout layoutFriendResult;

    private Button btnCopyCode;
    private Button btnFindFriend;
    private Button btnFriendRequest;

    private AppContainer container;
    private FriendRepository friendRepository;

    private String myFriendCode;
    private FriendCodeSearchDto selectedFriend;

    private Call<ProfileDto> profileCall;
    private Call<FriendCodeSearchDto> searchCall;
    private Call<FriendRequestResponseDto> requestCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_add);

        // 공통 네트워크 객체 가져오기
        container = DessertApplication.container(this);
        friendRepository = container.friendRepository;

        // XML 요소 연결
        tvMyFriendCode = findViewById(R.id.tv_my_friend_code);
        tvFriendName = findViewById(R.id.tv_friend_name);
        tvFriendCode = findViewById(R.id.tv_friend_code);
        tvFriendBio = findViewById(R.id.tv_friend_bio);

        etFriendCode = findViewById(R.id.et_friend_code);
        layoutFriendResult = findViewById(R.id.layout_friend_result);

        btnCopyCode = findViewById(R.id.btn_copy_code);
        btnFindFriend = findViewById(R.id.btn_find_friend);
        btnFriendRequest = findViewById(R.id.btn_friend_request);

        // 초기 화면 설정
        layoutFriendResult.setVisibility(View.GONE);
        btnCopyCode.setEnabled(false);
        btnFriendRequest.setEnabled(false);

        // 내 친구 코드 조회
        loadMyFriendCode();

        // 버튼 이벤트
        btnCopyCode.setOnClickListener(v -> copyMyFriendCode());
        btnFindFriend.setOnClickListener(v -> searchFriend());
        btnFriendRequest.setOnClickListener(v -> showRequestDialog());

        etFriendCode.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {

                // 입력값이 변경되면 기존 검색 결과 초기화
                selectedFriend = null;
                layoutFriendResult.setVisibility(View.GONE);
                btnFriendRequest.setEnabled(false);
                etFriendCode.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    // 1. 내 친구 코드 조회
    private void loadMyFriendCode() {

        tvMyFriendCode.setText("불러오는 중...");

        profileCall = container.repository.getProfile(
                new ApiCallback<ProfileDto>() {

                    @Override
                    public void onSuccess(ProfileDto data) {
                        if (isFinishing() || isDestroyed()) return;

                        myFriendCode = data.userFriendCode;

                        if (myFriendCode == null
                                || myFriendCode.trim().isEmpty()) {
                            tvMyFriendCode.setText("코드 없음");
                            return;
                        }

                        tvMyFriendCode.setText(myFriendCode);
                        btnCopyCode.setEnabled(true);
                    }

                    @Override
                    public void onError(ApiError error) {
                        if (isFinishing() || isDestroyed()) return;

                        tvMyFriendCode.setText("조회 실패");
                        showToast(error.userMessage);
                    }
                }
        );
    }

    // 2. 내 친구 코드 복사
    private void copyMyFriendCode() {

        if (myFriendCode == null) return;

        ClipboardManager clipboard =
                (ClipboardManager) getSystemService(
                        Context.CLIPBOARD_SERVICE
                );

        if (clipboard != null) {
            ClipData clip = ClipData.newPlainText(
                    "친구 코드", myFriendCode
            );

            clipboard.setPrimaryClip(clip);

            showToast("친구 코드가 복사되었습니다.");
        }
    }

    // 3. 친구 코드 검색
    private void searchFriend() {

        String inputCode =
                etFriendCode.getText().toString().trim();

        selectedFriend = null;
        layoutFriendResult.setVisibility(View.GONE);
        btnFriendRequest.setEnabled(false);
        etFriendCode.setError(null);

        // 빈 입력 확인
        if (inputCode.isEmpty()) {
            etFriendCode.setError("친구 코드를 입력해 주세요.");
            return;
        }

        // 친구 코드 형식 확인
        if (!inputCode.startsWith("#")) {
            etFriendCode.setError(
                    "#을 포함한 친구 코드를 입력해 주세요."
            );
            return;
        }

        // 본인 코드 확인
        if (inputCode.equals(myFriendCode)) {
            etFriendCode.setError("본인의 친구 코드입니다.");
            return;
        }

        btnFindFriend.setEnabled(false);
        etFriendCode.setEnabled(false);
        btnFindFriend.setText("검색 중...");

        searchCall = friendRepository.searchByFriendCode(
                inputCode,
                new ApiCallback<FriendCodeSearchDto>() {

                    @Override
                    public void onSuccess(FriendCodeSearchDto data) {
                        if (isFinishing() || isDestroyed()) return;

                        resetSearchButton();

                        selectedFriend = data;

                        tvFriendName.setText(data.nickname);
                        tvFriendCode.setText(data.friendCode);

                        tvFriendBio.setText(
                                data.description == null
                                        ? ""
                                        : data.description
                        );

                        layoutFriendResult.setVisibility(View.VISIBLE);
                        btnFriendRequest.setEnabled(true);
                        btnFriendRequest.setText("친구 요청");
                    }

                    @Override
                    public void onError(ApiError error) {
                        if (isFinishing() || isDestroyed()) return;

                        resetSearchButton();
                        showToast(error.userMessage);
                    }
                }
        );
    }

    // 4. 친구 요청 확인 팝업
    private void showRequestDialog() {

        if (selectedFriend == null) return;

        new AlertDialog.Builder(this)
                .setTitle("친구 요청")
                .setMessage(
                        selectedFriend.nickname
                                + "님에게 친구 요청을 보내시겠습니까?"
                )
                .setNegativeButton("취소", null)
                .setPositiveButton(
                        "요청하기",
                        (dialog, which) -> sendFriendRequest()
                )
                .show();
    }

    // 5. 실제 친구 요청 전송
    private void sendFriendRequest() {

        if (selectedFriend == null) return;

        int receiverId = selectedFriend.userId;

        btnFriendRequest.setEnabled(false);
        btnFriendRequest.setText("요청 중...");
        btnFindFriend.setEnabled(false);
        etFriendCode.setEnabled(false);

        requestCall = friendRepository.sendFriendRequest(
                receiverId,
                new ApiCallback<FriendRequestResponseDto>() {

                    @Override
                    public void onSuccess(FriendRequestResponseDto data) {
                        if (isFinishing() || isDestroyed()) return;

                        btnFriendRequest.setText("요청 완료");
                        btnFindFriend.setEnabled(true);
                        etFriendCode.setEnabled(true);

                        showToast("친구 요청을 보냈습니다.");
                    }

                    @Override
                    public void onError(ApiError error) {
                        if (isFinishing() || isDestroyed()) return;

                        btnFriendRequest.setEnabled(true);
                        btnFriendRequest.setText("친구 요청");
                        btnFindFriend.setEnabled(true);
                        etFriendCode.setEnabled(true);

                        showToast(error.userMessage);
                    }
                }
        );
    }

    private void resetSearchButton() {
        btnFindFriend.setEnabled(true);
        etFriendCode.setEnabled(true);
        btnFindFriend.setText("친구 찾기");
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

        if (profileCall != null) profileCall.cancel();
        if (searchCall != null) searchCall.cancel();
        if (requestCall != null) requestCall.cancel();

        super.onDestroy();
    }
}