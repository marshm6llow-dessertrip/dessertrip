package com.example.dessertrip.ui.community;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dessertrip.R;

import java.util.ArrayList;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private final List<Post> posts = new ArrayList<>();

    public void setPosts(List<Post> newPosts) {
        posts.clear();
        posts.addAll(newPosts);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, preview, meta;
        ViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.tv_post_title);
            preview = v.findViewById(R.id.tv_post_preview);
            meta = v.findViewById(R.id.tv_post_meta);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post p = posts.get(position);
        holder.title.setText(p.getPost_title());
        holder.preview.setText(p.getPost_content());
        holder.meta.setText("댓글 " + p.getComment_count() + "  ♡ " + p.getLike_count()
                + "  " + p.getCreated_at());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), PostDetailActivity.class);
            intent.putExtra("post_id", p.getPost_id());
            intent.putExtra("post_category_id", p.getPost_category_id());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }
}