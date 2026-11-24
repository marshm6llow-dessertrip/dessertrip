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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dessertrip.R;

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

    // TODO: API 연동 시 테스트용 데이터 제거
    private static final String TEST_MY_FRIEND_CODE = "#2343254765";
    private static final String TEST_FRIEND_CODE = "#5820391647";
    private static final String TEST_FRIEND_NAME = "까눌레수집가";
    private static final String TEST_FRIEND_BIO = "바삭한 첫 입을 찾아다녀요.";

    private boolean friendFound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_add);

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

        // 내 친구 코드 설정
        tvMyFriendCode.setText(TEST_MY_FRIEND_CODE);

        // 처음에는 검색 결과를 숨김
        layoutFriendResult.setVisibility(View.GONE);

        // 1. 내 친구 코드 복사
        btnCopyCode.setOnClickListener(v -> {

            String myCode = tvMyFriendCode.getText().toString();

            ClipboardManager clipboard =
                    (ClipboardManager) getSystemService(
                            Context.CLIPBOARD_SERVICE
                    );

            if (clipboard != null) {

                ClipData clip = ClipData.newPlainText(
                        "친구 코드", myCode
                );

                clipboard.setPrimaryClip(clip);

                Toast.makeText(
                        this,
                        "친구 코드가 복사되었습니다.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        // 2. 친구 코드로 검색
        btnFindFriend.setOnClickListener(v -> {

            String inputCode =
                    etFriendCode.getText().toString().trim();

            friendFound = false;
            layoutFriendResult.setVisibility(View.GONE);

            // 빈 입력 확인
            if (inputCode.isEmpty()) {
                etFriendCode.setError(
                        "친구 코드를 입력해 주세요."
                );
                return;
            }

            // 친구 코드 형식 확인
            if (!inputCode.startsWith("#")) {
                etFriendCode.setError(
                        "#을 포함한 친구 코드를 입력해 주세요."
                );
                return;
            }

            // 본인 코드 입력 여부 확인
            if (inputCode.equals(
                    tvMyFriendCode.getText().toString()
            )) {
                etFriendCode.setError(
                        "본인의 친구 코드입니다."
                );
                return;
            }

            // TODO: 친구 검색 API 연동
            if (inputCode.equals(TEST_FRIEND_CODE)) {

                tvFriendName.setText(TEST_FRIEND_NAME);
                tvFriendCode.setText(TEST_FRIEND_CODE);
                tvFriendBio.setText(TEST_FRIEND_BIO);

                friendFound = true;
                layoutFriendResult.setVisibility(View.VISIBLE);

                Toast.makeText(
                        this,
                        "친구를 찾았습니다.",
                        Toast.LENGTH_SHORT
                ).show();

            } else {

                Toast.makeText(
                        this,
                        "검색 결과가 없습니다.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        // 3. 친구 요청
        btnFriendRequest.setOnClickListener(v -> {

            if (!friendFound) {
                return;
            }

            new AlertDialog.Builder(this)
                    .setTitle("친구 요청")
                    .setMessage(
                            tvFriendName.getText().toString()
                                    + "님에게 친구 요청을 보내시겠습니까?"
                    )
                    .setNegativeButton(
                            "취소",
                            (dialog, which) -> dialog.dismiss()
                    )
                    .setPositiveButton("요청하기", (dialog, which) -> {

                        // TODO: 친구 요청 API 연동
                        Toast.makeText(
                                this,
                                "테스트용 친구 요청입니다. 실제로 전송되지는 않았습니다.",
                                Toast.LENGTH_LONG
                        ).show();
                    })
                    .show();
        });
    }
}