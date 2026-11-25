package com.dessertrip.dessertrip_backend.service.friend;


import com.dessertrip.dessertrip_backend.common.ApiException;
import com.dessertrip.dessertrip_backend.common.ErrorCode;
import com.dessertrip.dessertrip_backend.dto.friend.FriendRequestCreateRequest;
import com.dessertrip.dessertrip_backend.dto.friend.FriendRequestCreateResponse;
import com.dessertrip.dessertrip_backend.entity.collect.User;
import com.dessertrip.dessertrip_backend.entity.friend.FriendRequest;
import com.dessertrip.dessertrip_backend.entity.friend.FriendRequestStatus;
import com.dessertrip.dessertrip_backend.repository.collect.UserRepository;
import com.dessertrip.dessertrip_backend.repository.friend.FriendRepository;
import com.dessertrip.dessertrip_backend.repository.friend.FriendRequestRepository;
import com.dessertrip.dessertrip_backend.service.collect.CurrentUserService;
import com.dessertrip.dessertrip_backend.dto.friend.FriendCodeSearchResponse;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FriendService {

    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final FriendRequestRepository friendRequestRepository;

    public FriendService(
            CurrentUserService currentUserService,
            UserRepository userRepository,
            FriendRepository friendRepository,
            FriendRequestRepository friendRequestRepository
    ) {
        this.currentUserService = currentUserService;
        this.userRepository = userRepository;
        this.friendRepository = friendRepository;
        this.friendRequestRepository = friendRequestRepository;
    }

    @Transactional
    public FriendRequestCreateResponse sendFriendRequest(
            FriendRequestCreateRequest request
    ) {
        User sender = currentUserService.get();

        // 자기 자신에게 친구 요청 방지
        if (sender.getId().equals(request.receiverId())) {
            throw new ApiException(
                    ErrorCode.VALIDATION_FAILED,
                    "자기 자신에게 친구 요청을 보낼 수 없습니다."
            );
        }

        // 요청받을 사용자 조회
        User receiver = userRepository.findById(request.receiverId())
                .orElseThrow(() -> new ApiException(
                        ErrorCode.NOT_FOUND,
                        "해당 사용자를 찾을 수 없습니다."
                ));

        // 이미 친구인지 확인
        if (friendRepository.findFriendship(
                sender.getId(), receiver.getId()).isPresent()) {
            throw new ApiException(
                    ErrorCode.VALIDATION_FAILED,
                    "이미 친구로 등록된 사용자입니다."
            );
        }

        // 대기 중인 친구 요청 확인
        long pendingCount =
                friendRequestRepository.countRequestsBetweenUsers(
                        sender.getId(),
                        receiver.getId(),
                        FriendRequestStatus.PENDING
                );

        if (pendingCount > 0) {
            throw new ApiException(
                    ErrorCode.VALIDATION_FAILED,
                    "이미 대기 중인 친구 요청이 있습니다."
            );
        }

        // 친구 요청 생성 및 저장
        FriendRequest friendRequest =
                new FriendRequest(sender, receiver);

        FriendRequest saved =
                friendRequestRepository.saveAndFlush(friendRequest);

        // 응답 데이터 반환
        return new FriendRequestCreateResponse(
                saved.getId(),
                receiver.getId(),
                saved.getStatus(),
                saved.getCreatedAt()
        );

    }
    @Transactional(readOnly = true)
    public FriendCodeSearchResponse searchByFriendCode(String friendCode) {

        User currentUser = currentUserService.get();

        // 친구 코드 입력값 검증
        if (friendCode == null || friendCode.isBlank()) {
            throw new ApiException(
                    ErrorCode.VALIDATION_FAILED,
                    "친구 코드를 입력해 주세요."
            );
        }

        // 친구 코드로 사용자 조회
        User targetUser = userRepository.findByFriendCode(friendCode.trim())
                .orElseThrow(() -> new ApiException(
                        ErrorCode.NOT_FOUND,
                        "해당 친구 코드의 사용자를 찾을 수 없습니다."
                ));

        // 자기 자신 검색 방지
        if (currentUser.getId().equals(targetUser.getId())) {
            throw new ApiException(
                    ErrorCode.VALIDATION_FAILED,
                    "자기 자신은 검색할 수 없습니다."
            );
        }

        // 검색 결과 반환
        return new FriendCodeSearchResponse(
                targetUser.getId(),
                targetUser.getName(),
                targetUser.getFriendCode(),
                targetUser.getDescription(),
                targetUser.getProfileImgUrl()
        );
    }
}