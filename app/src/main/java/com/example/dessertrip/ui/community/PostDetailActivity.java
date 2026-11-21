package com.example.dessertrip.ui.community;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dessertrip.R;

public class PostDetailActivity extends AppCompatActivity {

    // post_category_id 값 (DB 명세서 기준)
    public static final int CATEGORY_FREE = 1;   // 자유게시글
    public static final int CATEGORY_NOTE = 2;   // 테이스팅 노트 공유글

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // 피드에서 넘겨준 값. 안 넘어오면 자유글(1)로 처리
        int categoryId = getIntent().getIntExtra("post_category_id", CATEGORY_FREE);

        if (categoryId == CATEGORY_NOTE) {
            setContentView(R.layout.activity_post_tasting_note_detail);
        } else {
            setContentView(R.layout.activity_post_detail);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}