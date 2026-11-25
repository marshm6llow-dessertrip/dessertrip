package com.example.dessertrip.ui.community;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.dessertrip.R;

public class CommunityFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // fragment_community.xml을 이 Fragment의 화면으로 사용
        View view = inflater.inflate(R.layout.fragment_community, container, false);

        // 더보기 클릭
        view.findViewById(R.id.tv_more_posts).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), PostListActivity.class)));

        view.findViewById(R.id.post_item_1).setOnClickListener(v -> {
            Intent i = new Intent(requireContext(), PostDetailActivity.class);
            i.putExtra("post_id", 1);
            i.putExtra("post_category_id", 1);
            startActivity(i);
        });
        return view;
    }
}