package com.example.dessertrip.ui.record;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.fragment.NavHostFragment;

import com.example.dessertrip.R;

/**
 * 기록하기 플로우(촬영/갤러리 → 스티커 → 노트 → 저장 완료)를 담는 화면.
 * 내부 이동은 res/navigation/register_graph.xml 로 처리한다.
 *
 * 시작 방법
 *  - 하단 '기록하기' 탭: 그냥 startActivity (진입 화면부터 시작)
 *  - 도감에서 기존 항목의 노트 작성/수정: EXTRA_STICKER_ID 를 넣어 startActivity (노트 화면부터 시작)
 */
public class RecordActivity extends AppCompatActivity {

    public static final String EXTRA_STICKER_ID = "stickerId";

    /** 기록 플로우를 끝내고 도감 탭으로 돌아가게 한다 (MainActivity 가 RESULT_OK 를 받으면 도감을 보여 준다). */
    public static void finishToArchive(android.app.Activity activity) {
        activity.setResult(android.app.Activity.RESULT_OK);
        activity.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_record);

        final View root = findViewById(R.id.root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        NavHostFragment host = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host);
        NavController nav = host.getNavController();

        // 그래프는 매번 설정해야 한다. 화면 회전 등으로 복원될 때는 NavController 가 저장된 백스택을 되살리고
        // 여기서 정한 시작 화면/인자는 무시한다.
        NavGraph graph = nav.getNavInflater().inflate(R.navigation.register_graph);
        Bundle args = null;
        long stickerId = getIntent().getLongExtra(EXTRA_STICKER_ID, -1L);
        if (stickerId != -1L) {
            graph.setStartDestination(R.id.noteFragment);
            args = new Bundle();
            args.putLong("stickerId", stickerId);
        }
        nav.setGraph(graph, args);

        // 카메라 화면만 검정 배경
        nav.addOnDestinationChangedListener((c, d, a) -> root.setBackgroundColor(
                d.getId() == R.id.cameraFragment ? Color.BLACK : ContextCompat.getColor(this, R.color.cream)));
    }
}
