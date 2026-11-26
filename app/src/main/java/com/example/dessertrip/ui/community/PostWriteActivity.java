package com.example.dessertrip.ui.community;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dessertrip.R;

public class PostWriteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_post_write);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        EditText etTitle = findViewById(R.id.et_title);
        EditText etContent = findViewById(R.id.et_content);
        TextView tvHeader = findViewById(R.id.tv_write_title);
        TextView tvSubmit = findViewById(R.id.tv_submit);

        // post_id가 넘어오면 수정 모드, 아니면 작성 모드
        int postId = getIntent().getIntExtra("post_id", -1);
        boolean isEdit = postId != -1;

        if (isEdit) {
            Post post = PostRepository.findById(postId);
            if (post == null) {
                finish();
                return;
            }
            tvHeader.setText("게시글 수정");
            tvSubmit.setText("수정하기");
            etTitle.setText(post.getPost_title());
            etContent.setText(post.getPost_content());
        }

        findViewById(R.id.tv_back).setOnClickListener(v -> finish());

        tvSubmit.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String content = etContent.getText().toString().trim();

            if (title.isEmpty()) {
                Toast.makeText(this, "제목을 입력해 주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (content.isEmpty()) {
                Toast.makeText(this, "내용을 입력해 주세요", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isEdit) {
                PostRepository.update(postId, title, content);
                Toast.makeText(this, "게시글을 수정했어요", Toast.LENGTH_SHORT).show();
            } else {
                PostRepository.add(title, content);
                Toast.makeText(this, "게시글을 등록했어요", Toast.LENGTH_SHORT).show();
            }
            finish();   // 이전 화면(목록/상세)의 onResume에서 갱신됨
        });
    }
}