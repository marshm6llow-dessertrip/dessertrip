package com.dessertrip.dessertrip_backend.repository.collect;

import com.dessertrip.dessertrip_backend.entity.collect.PostCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostCategoryRepository extends JpaRepository<PostCategory, Integer> {

    Optional<PostCategory> findByName(String name);
}
