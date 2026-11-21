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

        // 미리보기 글 클릭 → 게시글 상세 화면 (확인용)
        view.findViewById(R.id.post_item_1).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), PostDetailActivity.class)));
        view.findViewById(R.id.post_item_2).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), PostDetailActivity.class)));
        view.findViewById(R.id.post_item_3).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), PostDetailActivity.class)));
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, bars.top, 0, 0);
            return insets;
        });
        return view;
    }
}