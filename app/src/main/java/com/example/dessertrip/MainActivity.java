package com.example.dessertrip;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.dessertrip.ui.archive.collect.ArchiveFragment;
import com.example.dessertrip.ui.community.CommunityFragment;
import com.example.dessertrip.ui.explore.ExploreFragment;
import com.example.dessertrip.ui.my.MyFragment;
import com.example.dessertrip.ui.record.RecordActivity;
import com.example.dessertrip.databinding.SheetRecordMenuBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNav;
    private ActivityResultLauncher<Intent> recordLauncher;

    /** 가운데 + 버튼: "디저트 스티커 생성" / "커뮤니티 게시글 작성" 선택 시트 */
    private void showRecordMenu() {
        final BottomSheetDialog dialog = new BottomSheetDialog(this);
        SheetRecordMenuBinding m = SheetRecordMenuBinding.inflate(getLayoutInflater());
        dialog.setContentView(m.getRoot());
        m.stickerOption.setOnClickListener(v -> {
            dialog.dismiss();
            recordLauncher.launch(new Intent(this, RecordActivity.class));
        });
        m.postOption.setOnClickListener(v -> {
            dialog.dismiss();
            // TODO: 커뮤니티 게시글 작성 화면이 만들어지면 연결
            Toast.makeText(this, "커뮤니티 게시글 작성은 곧 만나요!", Toast.LENGTH_SHORT).show();
        });
        dialog.show();
    }

    /** 기록 플로우가 끝나면 도감 탭을 (새로 불러와서) 보여 준다. */
    private void showArchive() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new ArchiveFragment()).commit();
        bottomNav.getMenu().findItem(R.id.nav_archive).setChecked(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        bottomNav = nav;
        recordLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), r -> {
            if (r.getResultCode() == RESULT_OK) showArchive();
        });
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_record) {
                showRecordMenu();
                return false; // 탭 선택 상태로 만들지 않음
            }
            Fragment f;
            if (id == R.id.nav_explore) f = new ExploreFragment();
            else if (id == R.id.nav_archive) f = new ArchiveFragment();
            else if (id == R.id.nav_community) f = new CommunityFragment();
            else f = new MyFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, f).commit();
            return true;
        });

        if (savedInstanceState == null) nav.setSelectedItemId(R.id.nav_explore);
    }
}