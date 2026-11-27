package com.example.dessertrip.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dessertrip.MainActivity;
import com.example.dessertrip.R;
import com.example.dessertrip.databinding.ActivitySignupBinding;

import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {

    private static final Pattern PW_PATTERN =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$");
    private static final int COLOR_HELP = 0xFF8A7D73;
    private static final int COLOR_ERROR = 0xFFD04C54;

    private ActivitySignupBinding b;
    private boolean updatingChecks = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        b.btnBack.setOnClickListener(v -> finish());
        b.tvGoLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {
            }

            @Override
            public void onTextChanged(CharSequence s, int st, int b2, int c) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                validateAll();
            }
        };
        b.etEmail.addTextChangedListener(watcher);
        b.etNickname.addTextChangedListener(watcher);
        b.etPassword.addTextChangedListener(watcher);
        b.etPasswordConfirm.addTextChangedListener(watcher);

        b.ivPwToggle.setOnClickListener(v -> togglePassword(b.etPassword, b.ivPwToggle));
        b.ivConfirmToggle.setOnClickListener(v -> togglePassword(b.etPasswordConfirm, b.ivConfirmToggle));
        setupAgreements();

        b.btnSubmit.setOnClickListener(v -> {
            // TODO: 서버 회원가입 요청. 성공 시 아래 실행
            Toast.makeText(this, "가입을 환영해요!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void togglePassword(android.widget.EditText et, android.widget.ImageView icon) {
        boolean visible = et.getTransformationMethod() instanceof HideReturnsTransformationMethod;
        et.setTransformationMethod(visible
                ? PasswordTransformationMethod.getInstance()
                : HideReturnsTransformationMethod.getInstance());
        et.setSelection(et.getText().length());
        icon.setImageResource(visible ? R.drawable.ic_visibility_off : R.drawable.ic_visibility);
    }

    private void setupAgreements() {
        final CheckBox[] items = {b.cbAge, b.cbTerms, b.cbPrivacy, b.cbMarketing};

        b.cbAll.setOnCheckedChangeListener((btn, checked) -> {
            if (updatingChecks) return;
            updatingChecks = true;
            for (CheckBox cb : items) cb.setChecked(checked);
            updatingChecks = false;
            validateAll();
        });

        for (CheckBox cb : items) {
            cb.setOnCheckedChangeListener((btn, checked) -> {
                if (updatingChecks) return;
                boolean all = true;
                for (CheckBox c : items) all &= c.isChecked();
                updatingChecks = true;
                b.cbAll.setChecked(all);
                updatingChecks = false;
                validateAll();
            });
        }
    }

    private void validateAll() {
        String email = b.etEmail.getText().toString().trim();
        String nick = b.etNickname.getText().toString().trim();
        String pw = b.etPassword.getText().toString();
        String confirm = b.etPasswordConfirm.getText().toString();

        // 이메일
        boolean emailOk = Patterns.EMAIL_ADDRESS.matcher(email).matches();
        b.ivEmailCheck.setVisibility(emailOk ? View.VISIBLE : View.INVISIBLE);
        setHelp(b.tvEmailHelp, email.isEmpty() || emailOk ? "" : "이메일 형식을 확인해 주세요.",
                !email.isEmpty() && !emailOk);

        // 닉네임 (TODO: 서버 중복 확인)
        boolean nickOk = nick.length() >= 2 && nick.length() <= 10;
        b.ivNicknameCheck.setVisibility(nickOk ? View.VISIBLE : View.INVISIBLE);
        if (nickOk) setHelp(b.tvNicknameHelp, "사용할 수 있는 닉네임이에요.", false);
        else setHelp(b.tvNicknameHelp, "2~10자로 입력해 주세요.", !nick.isEmpty());

        // 비밀번호
        boolean pwOk = PW_PATTERN.matcher(pw).matches();
        setHelp(b.tvPwHelp, "영문, 숫자, 특수문자를 포함한 8자 이상이에요.", !pw.isEmpty() && !pwOk);

        // 비밀번호 확인
        boolean confirmMatch = !confirm.isEmpty() && pw.equals(confirm);
        boolean confirmOk = pwOk && confirmMatch;
        b.ivConfirmCheck.setVisibility(confirmMatch ? View.VISIBLE : View.INVISIBLE);
        setHelp(b.tvConfirmHelp, !confirm.isEmpty() && !confirmMatch ? "비밀번호가 일치하지 않아요." : "",
                !confirm.isEmpty() && !confirmMatch);

        // 필수 약관
        boolean requiredOk = b.cbAge.isChecked() && b.cbTerms.isChecked() && b.cbPrivacy.isChecked();

        boolean enabled = emailOk && nickOk && pwOk && confirmOk && requiredOk;
        b.btnSubmit.setEnabled(enabled);
        b.btnSubmit.setAlpha(enabled ? 1f : 0.5f);
    }

    private void setHelp(android.widget.TextView tv, String text, boolean error) {
        tv.setText(text);
        tv.setVisibility(text.isEmpty() ? View.GONE : View.VISIBLE);
        tv.setTextColor(error ? COLOR_ERROR : COLOR_HELP);
    }
}