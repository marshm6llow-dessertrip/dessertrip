package com.example.dessertrip.ui.community;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dessertrip.R;

public class PostListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_list);

        // 뒤로 가기
        findViewById(R.id.tv_back).setOnClickListener(v -> finish());
    }
}