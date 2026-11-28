package com.dessertrip.dessertrip_backend.repository.friend;

import com.dessertrip.dessertrip_backend.entity.friend.FriendRequest;
import com.dessertrip.dessertrip_backend.entity.friend.FriendRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FriendRequestRepository
        extends JpaRepository<FriendRequest, Integer> {

    List<FriendRequest> findAllByReceiver_IdAndStatusOrderByCreatedAtDesc(
            Integer receiverId,
            FriendRequestStatus status
    );

    @Query("""
        SELECT COUNT(fr) FROM FriendRequest fr
        WHERE fr.status = :status
          AND (
              (fr.sender.id = :userId AND fr.receiver.id = :otherUserId)
              OR
              (fr.sender.id = :otherUserId AND fr.receiver.id = :userId)
          )
        """)
    long countRequestsBetweenUsers(
            @Param("userId") Integer userId,
            @Param("otherUserId") Integer otherUserId,
            @Param("status") FriendRequestStatus status
    );
}