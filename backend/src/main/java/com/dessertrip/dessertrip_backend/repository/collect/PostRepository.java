package com.dessertrip.dessertrip_backend.repository.collect;

import com.dessertrip.dessertrip_backend.entity.collect.Post;
import com.dessertrip.dessertrip_backend.entity.collect.PostCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Integer> {

    List<Post> findByDessertIdAndPostCategory(Integer dessertId, PostCategory postCategory);

    boolean existsByDessertIdAndPostCategory(Integer dessertId, PostCategory postCategory);

    /** 게시글(스냅샷)이 이 이미지 파일을 쓰고 있는지 — 쓰고 있으면 원본 노트가 바뀌어도 파일을 지우면 안 된다 */
    @Query("select count(i) > 0 from PostImage i where i.imageUrl = :file")
    boolean isImageInUse(@Param("file") String file);

    /** 게시글이 이 스티커를 가져다 쓰고 있는지 */
    boolean existsByStickerId(Integer stickerId);

    /** 내 디저트 중 커뮤니티에 공유된(= 테이스팅 노트 게시글이 있는) 디저트 id 들 */
    @Query("select p.dessertId from Post p where p.userId = :userId and p.postCategory = :category and p.dessertId is not null")
    List<Integer> findSharedDessertIds(@Param("userId") Integer userId, @Param("category") PostCategory category);
}
