package com.example.dessertrip.ui.community;

public class Post {
    private final int post_id;
    private final int post_category_id;   // 1=자유글, 2=테이스팅 노트
    private String post_title;
    private String post_content;
    private final String author_name;
    private int like_count;
    private int comment_count;
    private final String created_at;

    public Post(int post_id, int post_category_id, String post_title, String post_content,
                String author_name, int like_count, int comment_count, String created_at) {
        this.post_id = post_id;
        this.post_category_id = post_category_id;
        this.post_title = post_title;
        this.post_content = post_content;
        this.author_name = author_name;
        this.like_count = like_count;
        this.comment_count = comment_count;
        this.created_at = created_at;
    }

    public int getPost_id() { return post_id; }
    public int getPost_category_id() { return post_category_id; }
    public String getPost_title() { return post_title; }
    public String getPost_content() { return post_content; }
    public String getAuthor_name() { return author_name; }
    public int getLike_count() { return like_count; }
    public int getComment_count() { return comment_count; }
    public String getCreated_at() { return created_at; }

    public void setPost_title(String t) { this.post_title = t; }
    public void setPost_content(String c) { this.post_content = c; }
}