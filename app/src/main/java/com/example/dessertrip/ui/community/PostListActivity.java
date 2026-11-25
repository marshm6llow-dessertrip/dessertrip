package com.example.dessertrip.ui.community;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dessertrip.R;

public class PostListActivity extends AppCompatActivity {

    private PostAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_post_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        adapter = new PostAdapter();
        RecyclerView rv = findViewById(R.id.rv_post_list);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        findViewById(R.id.tv_back).setOnClickListener(v -> finish());
        findViewById(R.id.tv_write).setOnClickListener(v ->
                startActivity(new Intent(this, PostWriteActivity.class)));
    }

    // 작성·수정·삭제 후 돌아왔을 때 목록 갱신
    @Override
    protected void onResume() {
        super.onResume();
        adapter.setPosts(PostRepository.getAll());
    }
}