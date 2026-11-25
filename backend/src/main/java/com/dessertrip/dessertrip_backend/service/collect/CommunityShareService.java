package com.dessertrip.dessertrip_backend.service.collect;

import com.dessertrip.dessertrip_backend.entity.collect.Post;
import com.dessertrip.dessertrip_backend.entity.collect.PostCategory;
import com.dessertrip.dessertrip_backend.repository.collect.PostCategoryRepository;
import com.dessertrip.dessertrip_backend.repository.collect.PostRepository;
import com.dessertrip.dessertrip_backend.entity.collect.Dessert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * "커뮤니티에 공유하기": 테이스팅 노트의 내용을 복사해 게시글(post) 한 개를 만든다.
 * 게시글은 공유한 시점의 스냅샷이다. 노트를 나중에 고치거나 지워도 게시글은 바뀌지 않고, 이미 공유된 노트는 다시 공유해도 새 게시글을 만들지 않는다.
 * 별도 "공유 여부" 컬럼은 없다: 테이스팅 노트 게시글이 있으면 공유 중이다.
 * 제목은 API 문서 규칙대로 "{작성자 닉네임}님이 테이스팅 노트를 작성했어요!" 이다.
 */
@Service
public class CommunityShareService {

    static final String TASTING_NOTE = "테이스팅 노트";

    private final PostRepository postRepository;
    private final PostCategoryRepository postCategoryRepository;

    public CommunityShareService(PostRepository postRepository, PostCategoryRepository postCategoryRepository) {
        this.postRepository = postRepository;
        this.postCategoryRepository = postCategoryRepository;
    }

    /** 노트를 저장한 뒤 "공유하기"가 켜져 있으면 호출: 아직 게시글이 없을 때만 스냅샷 게시글을 만든다. */
    @Transactional
    public void shareIfAbsent(Dessert dessert, String authorName) {
        PostCategory category = tastingNoteCategory();
        if (postRepository.existsByDessertIdAndPostCategory(dessert.getId(), category)) return;
        Post post = new Post(dessert.getUserId(), category, authorName + "님이 테이스팅 노트를 작성했어요!");
        post.copyFrom(dessert);
        postRepository.save(post);
    }

    /** 게시글이 이 파일(이미지)을 쓰고 있는지 */
    @Transactional(readOnly = true)
    public boolean usesImage(String fileName) {
        return postRepository.isImageInUse(fileName);
    }

    /** 게시글이 이 스티커를 쓰고 있는지 */
    @Transactional(readOnly = true)
    public boolean usesSticker(Integer stickerId) {
        return postRepository.existsByStickerId(stickerId);
    }

    @Transactional(readOnly = true)
    public boolean isShared(Dessert dessert) {
        return postCategoryRepository.findByName(TASTING_NOTE)
                .map(c -> postRepository.existsByDessertIdAndPostCategory(dessert.getId(), c))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public Set<Integer> sharedDessertIds(Integer userId) {
        return postCategoryRepository.findByName(TASTING_NOTE)
                .map(c -> new HashSet<>(postRepository.findSharedDessertIds(userId, c)))
                .orElseGet(HashSet::new);
    }

    private PostCategory tastingNoteCategory() {
        return postCategoryRepository.findByName(TASTING_NOTE)
                .orElseGet(() -> postCategoryRepository.save(new PostCategory(TASTING_NOTE, "테이스팅 노트를 커뮤니티에 공유한 글")));
    }
}
