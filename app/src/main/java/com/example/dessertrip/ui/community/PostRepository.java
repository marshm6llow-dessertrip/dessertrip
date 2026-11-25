package com.example.dessertrip.ui.community;

import java.util.ArrayList;
import java.util.List;

public class PostRepository {

    private static final List<Post> posts = new ArrayList<>();
    private static int nextId = 100;

    static {
        posts.add(new Post(1, 1, "소금빵 맛있는 집 추천해주세요!",
                "연남동 근처에서 소금빵 잘하는 카페 아시는 분 계신가요?", "다은", 7, 3, "1시간 전"));
        posts.add(new Post(2, 2, "다은님이 테이스팅 노트를 작성했어요!",
                "크림이 구름처럼 가벼워요.", "다은", 12, 5, "30분 전"));
        posts.add(new Post(3, 1, "홍대 마카롱 맛집 비교 후기",
                "세 곳을 직접 비교해봤어요.", "버터산책", 15, 6, "어제"));
    }

    public static List<Post> getAll() {
        return new ArrayList<>(posts);
    }

    public static Post findById(int id) {
        for (Post p : posts) {
            if (p.getPost_id() == id) return p;
        }
        return null;
    }

    public static void add(String title, String content) {
        posts.add(0, new Post(nextId++, 1, title, content, "소은", 0, 0, "방금 전"));
    }

    public static void update(int id, String title, String content) {
        Post p = findById(id);
        if (p != null) {
            p.setPost_title(title);
            p.setPost_content(content);
        }
    }

    public static void delete(int id) {
        Post p = findById(id);
        if (p != null) posts.remove(p);
    }
}