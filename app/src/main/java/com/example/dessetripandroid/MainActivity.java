package com.example.dessetripandroid;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.dessetripandroid.ui.archive.ArchiveFragment;
import com.example.dessetripandroid.ui.community.CommunityFragment;
import com.example.dessetripandroid.ui.explore.ExploreFragment;
import com.example.dessetripandroid.ui.my.MyFragment;
import com.example.dessetripandroid.ui.record.RecordActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_record) {
                startActivity(new Intent(this, RecordActivity.class));
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