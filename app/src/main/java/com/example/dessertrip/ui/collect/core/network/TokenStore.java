package com.example.dessertrip.ui.collect.core.network;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 인증 방식/토큰 처리 방식은 서버 담당자가 설계 후 공유 → 확정되면 이 파일/AuthInterceptor 만 수정.
 * 현재는 "Authorization: Bearer <accessToken>" 을 가정한 임시 구현이다.
 * (운영 배포 전 EncryptedSharedPreferences/Keystore 등 안전한 저장소로 교체할 것)
 */
public class TokenStore {
    private static final String KEY_ACCESS = "access_token";
    private final SharedPreferences prefs;

    public TokenStore(Context context) {
        prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
    }

    public String getAccessToken() { return prefs.getString(KEY_ACCESS, null); }
    public void setAccessToken(String token) { prefs.edit().putString(KEY_ACCESS, token).apply(); }
    public void clear() { prefs.edit().clear().apply(); }
}
