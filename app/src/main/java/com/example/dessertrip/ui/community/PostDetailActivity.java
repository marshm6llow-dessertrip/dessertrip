package com.example.dessertrip.ui.community;

import android.os.Bundle;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dessertrip.R;

public class PostDetailActivity extends AppCompatActivity {

    public static final int CATEGORY_FREE = 1;   // 자유게시글
    public static final int CATEGORY_NOTE = 2;   // 테이스팅 노트 공유글

    private int postId;
    private int categoryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        postId = getIntent().getIntExtra("post_id", -1);
        categoryId = getIntent().getIntExtra("post_category_id", CATEGORY_FREE);

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

        findViewById(R.id.tv_back).setOnClickListener(v -> finish());
        findViewById(R.id.tv_menu).setOnClickListener(this::showMenu);
    }

    // 수정 화면에서 돌아왔을 때 최신 내용 반영
    @Override
    protected void onResume() {
        super.onResume();
        if (categoryId != CATEGORY_FREE || postId == -1) return;   // 노트글·확인용 진입은 건너뜀

        Post post = PostRepository.findById(postId);
        if (post == null) {
            finish();
            return;
        }
        ((TextView) findViewById(R.id.tv_post_title)).setText(post.getPost_title());
        ((TextView) findViewById(R.id.tv_post_content)).setText(post.getPost_content());
        ((TextView) findViewById(R.id.tv_author_nickname)).setText(post.getAuthor_name());
        ((TextView) findViewById(R.id.tv_post_date)).setText(post.getCreated_at());
        ((TextView) findViewById(R.id.tv_like_count)).setText(String.valueOf(post.getLike_count()));
        ((TextView) findViewById(R.id.tv_comment_count)).setText(String.valueOf(post.getComment_count()));
    }

    private void showMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(0, 1, 0, "게시물 수정");
        popup.getMenu().add(0, 2, 1, "게시물 삭제");
        popup.setOnMenuItemClickListener(item -> {
            if (categoryId == CATEGORY_NOTE) {
                showNoteGuideDialog();          // 노트 글은 커뮤니티에서 수정·삭제 불가
            } else if (postId == -1) {
                Toast.makeText(this, "확인용 화면이라 수정·삭제할 수 없어요", Toast.LENGTH_SHORT).show();
            } else if (item.getItemId() == 1) {
                Intent intent = new Intent(this, PostWriteActivity.class);
                intent.putExtra("post_id", postId);
                startActivity(intent);
            } else {
                showDeleteDialog();
            }
            return true;
        });
        popup.show();
    }

    private void showDeleteDialog() {
        new AlertDialog.Builder(this)
                .setMessage("게시물을 삭제 하시겠습니까?")
                .setNegativeButton("취소", null)
                .setPositiveButton("확인", (d, w) -> {
                    PostRepository.delete(postId);
                    Toast.makeText(this, "게시글을 삭제했어요", Toast.LENGTH_SHORT).show();
                    finish();   // 목록 onResume에서 갱신됨
                })
                .show();
    }

    private void showNoteGuideDialog() {
        new AlertDialog.Builder(this)
                .setMessage("테이스팅 노트로 작성한 글은 도감에서 수정/삭제할 수 있어요.")
                .setNegativeButton("확인", null)
                .setPositiveButton("도감으로 이동", (d, w) -> {
                    // TODO: 도감 화면 연결 후 이동 코드 추가
                })
                .show();
    }
}