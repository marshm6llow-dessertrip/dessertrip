package com.example.dessertrip.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dessertrip.MainActivity;
import com.example.dessertrip.R;
import com.example.dessertrip.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding b;
    private boolean pwVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        // 뒤로가기
        b.btnBack.setOnClickListener(v -> finish());

        // 비밀번호 보기 토글
        b.ivPwToggle.setOnClickListener(v -> togglePassword());

        // 로그인
        b.btnLogin.setOnClickListener(v -> attemptLogin());

        // 찾기 / 회원가입
        b.tvFindPw.setOnClickListener(v ->
                Toast.makeText(this, "비밀번호 찾기는 준비 중이에요", Toast.LENGTH_SHORT).show());
        b.tvFindId.setOnClickListener(v ->
                Toast.makeText(this, "아이디 찾기는 준비 중이에요", Toast.LENGTH_SHORT).show());
        b.tvGoSignUp.setOnClickListener(v -> {
            startActivity(new Intent(this, SignUpActivity.class));
            finish();
        });
    }

    private void togglePassword() {
        pwVisible = !pwVisible;
        if (pwVisible) {
            b.etPassword.setInputType(InputType.TYPE_CLASS_TEXT
                    | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            b.ivPwToggle.setImageResource(R.drawable.ic_visibility);
        } else {
            b.etPassword.setInputType(InputType.TYPE_CLASS_TEXT
                    | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            b.ivPwToggle.setImageResource(R.drawable.ic_visibility_off);
        }
        // 토글 후 커서가 맨 앞으로 가는 것 방지
        b.etPassword.setSelection(b.etPassword.getText().length());
    }

    private void attemptLogin() {
        String email = b.etEmail.getText().toString().trim();
        String pw = b.etPassword.getText().toString();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            b.etEmail.setError("이메일 형식을 확인해 주세요");
            b.etEmail.requestFocus();
            return;
        }
        if (pw.isEmpty()) {
            b.etPassword.setError("비밀번호를 입력해 주세요");
            b.etPassword.requestFocus();
            return;
        }

        // TODO: 서버/Firebase 로그인 연동 후 실제 검사로 교체 (현재는 임시 통과)
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}