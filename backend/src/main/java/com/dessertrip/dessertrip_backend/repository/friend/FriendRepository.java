package com.dessertrip.dessertrip_backend.repository.friend;

import com.dessertrip.dessertrip_backend.entity.friend.Friend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Integer> {

    @Query("""
        SELECT f FROM Friend f
        WHERE f.user.id = :userId
           OR f.friendUser.id = :userId
        """)
    List<Friend> findAllByUserId(@Param("userId") Integer userId);

    @Query("""
        SELECT f FROM Friend f
        WHERE (f.user.id = :userId AND f.friendUser.id = :friendUserId)
           OR (f.user.id = :friendUserId AND f.friendUser.id = :userId)
        """)
    Optional<Friend> findFriendship(
            @Param("userId") Integer userId,
            @Param("friendUserId") Integer friendUserId
    );
}