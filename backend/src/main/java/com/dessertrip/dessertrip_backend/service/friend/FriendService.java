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
import com.dessertrip.dessertrip_backend.dto.friend.FriendRequestUpdateRequest;
import com.dessertrip.dessertrip_backend.dto.friend.FriendRequestUpdateResponse;
import com.dessertrip.dessertrip_backend.entity.friend.Friend;
import com.dessertrip.dessertrip_backend.dto.friend.FriendListItemResponse;
import com.dessertrip.dessertrip_backend.dto.friend.FriendListResponse;
import com.dessertrip.dessertrip_backend.dto.friend.FriendDeleteResponse;
import com.dessertrip.dessertrip_backend.dto.friend.UserSearchItemResponse;
import com.dessertrip.dessertrip_backend.dto.friend.UserSearchResponse;

import java.util.List;
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
    @Transactional
    public FriendRequestUpdateResponse updateFriendRequest(
            Integer requestId,
            FriendRequestUpdateRequest request
    ) {
        User currentUser = currentUserService.get();

        // 1. 허용된 요청 상태인지 확인
        if (request.status() != FriendRequestStatus.ACCEPTED
                && request.status() != FriendRequestStatus.REJECTED) {
            throw new ApiException(
                    ErrorCode.VALIDATION_FAILED,
                    "수락 또는 거절만 가능합니다."
            );
        }

        // 2. 친구 요청 조회
        FriendRequest friendRequest = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new ApiException(
                        ErrorCode.NOT_FOUND,
                        "해당 친구 요청을 찾을 수 없습니다."
                ));

        // 3. 요청을 받은 사용자 본인인지 확인
        if (!friendRequest.getReceiver().getId().equals(currentUser.getId())) {
            throw new ApiException(
                    ErrorCode.FORBIDDEN,
                    "해당 친구 요청을 처리할 권한이 없습니다."
            );
        }

        // 4. 이미 처리된 요청인지 확인
        if (friendRequest.getStatus() != FriendRequestStatus.PENDING) {
            throw new ApiException(
                    ErrorCode.VALIDATION_FAILED,
                    "이미 처리된 친구 요청입니다."
            );
        }

        User sender = friendRequest.getSender();
        User receiver = friendRequest.getReceiver();

        // 5. 수락 또는 거절 처리
        if (request.status() == FriendRequestStatus.ACCEPTED) {

            // 이미 친구 관계가 존재하는지 확인
            if (friendRepository.findFriendship(
                    sender.getId(), receiver.getId()).isPresent()) {
                throw new ApiException(
                        ErrorCode.VALIDATION_FAILED,
                        "이미 친구로 등록된 사용자입니다."
                );
            }

            // 친구 관계 생성
            friendRepository.save(new Friend(sender, receiver));

            // 요청 상태 변경
            friendRequest.accept();

        } else {

            // 요청 거절
            friendRequest.reject();
        }

        // 6. 변경 사항 DB 반영
        FriendRequest saved = friendRequestRepository.saveAndFlush(friendRequest);

        // 7. 응답 반환
        return new FriendRequestUpdateResponse(
                saved.getId(),
                saved.getStatus(),
                sender.getId(),
                saved.getStatus() == FriendRequestStatus.ACCEPTED,
                saved.getUpdatedAt()
        );
    }

    @Transactional(readOnly = true)
    public FriendListResponse getFriends() {
        User currentUser = currentUserService.get();

        List<Friend> friendships =
                friendRepository.findAllByUserId(currentUser.getId());

        List<FriendListItemResponse> friends = friendships.stream()
                .map(friendship -> {
                    User friendUser;

                    if (friendship.getUser().getId().equals(currentUser.getId())) {
                        friendUser = friendship.getFriendUser();
                    } else {
                        friendUser = friendship.getUser();
                    }

                    return new FriendListItemResponse(
                            friendUser.getId(),
                            friendUser.getName(),
                            friendUser.getProfileImgUrl()
                    );
                })
                .toList();

        return new FriendListResponse(friends);
    }
    @Transactional
    public FriendDeleteResponse deleteFriend(Integer friendId) {
        User currentUser = currentUserService.get();

        if (currentUser.getId().equals(friendId)) {
            throw new ApiException(
                    ErrorCode.VALIDATION_FAILED,
                    "자기 자신과의 친구 관계는 해제할 수 없습니다."
            );
        }

        Friend friendship = friendRepository
                .findFriendship(currentUser.getId(), friendId)
                .orElseThrow(() -> new ApiException(
                        ErrorCode.NOT_FOUND,
                        "해당 친구 관계를 찾을 수 없습니다."
                ));

        friendRepository.delete(friendship);
        friendRepository.flush();

        long friendCount = friendRepository
                .findAllByUserId(currentUser.getId())
                .size();

        return new FriendDeleteResponse(
                friendId,
                false,
                friendCount,
                "친구 관계가 해제되었습니다."
        );
    }

    @Transactional(readOnly = true)
    public UserSearchResponse searchByNickname(String keyword) {

        // 1. 검색어 검증
        if (keyword == null || keyword.isBlank()) {
            throw new ApiException(
                    ErrorCode.VALIDATION_FAILED,
                    "검색어를 입력해 주세요."
            );
        }

        // 2. 현재 사용자 조회
        User currentUser = currentUserService.get();

        // 3. 닉네임 부분 일치 검색 (자기 자신 제외)
        List<User> users = userRepository
                .findByNameContainingAndIdNotOrderByNameAsc(
                        keyword.trim(),
                        currentUser.getId()
                );

        // 4. 검색 결과를 응답 DTO로 변환
        List<UserSearchItemResponse> results = users.stream()
                .map(user -> new UserSearchItemResponse(
                        user.getId(),
                        user.getName(),
                        user.getProfileImgUrl()
                ))
                .toList();

        // 5. 응답 반환
        return new UserSearchResponse(results);
    }

}