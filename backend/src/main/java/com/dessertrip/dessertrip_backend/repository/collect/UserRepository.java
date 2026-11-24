package com.dessertrip.dessertrip_backend.repository.collect;

import com.dessertrip.dessertrip_backend.entity.collect.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    /** 로그인 연동 전까지의 "현재 사용자": 가장 먼저 만들어진 유저 */
    Optional<User> findFirstByOrderByIdAsc();
}
