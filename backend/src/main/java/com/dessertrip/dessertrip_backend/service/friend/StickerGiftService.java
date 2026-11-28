package com.dessertrip.dessertrip_backend.service.friend;

import com.dessertrip.dessertrip_backend.common.ApiException;
import com.dessertrip.dessertrip_backend.common.ErrorCode;
import com.dessertrip.dessertrip_backend.dto.friend.*;
import com.dessertrip.dessertrip_backend.entity.collect.Sticker;
import com.dessertrip.dessertrip_backend.entity.collect.User;
import com.dessertrip.dessertrip_backend.entity.friend.StickerShare;
import com.dessertrip.dessertrip_backend.entity.friend.StickerShareStatus;
import com.dessertrip.dessertrip_backend.repository.collect.StickerRepository;
import com.dessertrip.dessertrip_backend.repository.collect.UserRepository;
import com.dessertrip.dessertrip_backend.repository.friend.FriendRepository;
import com.dessertrip.dessertrip_backend.repository.friend.StickerShareRepository;
import com.dessertrip.dessertrip_backend.service.collect.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
@Service
public class StickerGiftService {

    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;
    private final StickerRepository stickerRepository;
    private final FriendRepository friendRepository;
    private final StickerShareRepository stickerShareRepository;
    private final StickerTransferService stickerTransferService;

    public StickerGiftService(
            CurrentUserService currentUserService,
            UserRepository userRepository,
            StickerRepository stickerRepository,
            FriendRepository friendRepository,
            StickerShareRepository stickerShareRepository,
            StickerTransferService stickerTransferService
    ) {
        this.currentUserService = currentUserService;
        this.userRepository = userRepository;
        this.stickerRepository = stickerRepository;
        this.friendRepository = friendRepository;
        this.stickerShareRepository = stickerShareRepository;
        this.stickerTransferService = stickerTransferService;
    }

    /**
     * 스티커 선물 보내기
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public StickerGiftCreateResponse sendGift(
            StickerGiftCreateRequest request
    ) {

        User sender = currentUserService.get();

        // 1. 자기 자신에게 선물할 수 없음
        if (sender.getId().equals(request.receiverId())) {
            throw new ApiException(
                    ErrorCode.VALIDATION_FAILED,
                    "자기 자신에게 선물할 수 없습니다."
            );
        }

        // 2. 수신자 존재 여부 확인
        User receiver = userRepository
                .findById(request.receiverId())
                .orElseThrow(() -> new ApiException(
                        ErrorCode.NOT_FOUND,
                        "선물을 받을 사용자를 찾을 수 없습니다."
                ));

        // 3. 원본 스티커 조회 및 잠금
        Sticker sticker = stickerRepository
                .findByIdForUpdate(request.stickerId())
                .orElseThrow(() -> new ApiException(
                        ErrorCode.STICKER_NOT_FOUND,
                        "해당 스티커를 찾을 수 없습니다."
                ));

        // 4. 본인이 소유한 스티커인지 확인
        if (!sender.getId().equals(sticker.getUserId())) {
            throw new ApiException(
                    ErrorCode.FORBIDDEN,
                    "본인이 소유한 스티커만 선물할 수 있습니다."
            );
        }

        // 5. 선물받은 스티커는 재선물 불가
        if (sticker.isPresent()) {
            throw new ApiException(
                    ErrorCode.VALIDATION_FAILED,
                    "선물받은 스티커는 다시 선물할 수 없습니다."
            );
        }

        // 6. 활성화된 친구 관계인지 확인
        if (friendRepository.findFriendship(
                sender.getId(), receiver.getId()
        ).isEmpty()) {
            throw new ApiException(
                    ErrorCode.VALIDATION_FAILED,
                    "친구에게만 스티커를 선물할 수 있습니다."
            );
        }

        // 7. 동일 친구에게 동일 스티커 반복 선물 금지
        if (stickerShareRepository
                .existsBySticker_IdAndReceiver_Id(
                        sticker.getId(),
                        receiver.getId()
                )) {
            throw new ApiException(
                    ErrorCode.VALIDATION_FAILED,
                    "이미 해당 친구에게 선물한 스티커입니다."
            );
        }

        // 8. 선택 입력 메시지 처리
        String message = request.message();

        if (message != null && message.isBlank()) {
            message = null;
        }

        // 9. 선물 요청 저장
        StickerShare gift = new StickerShare(
                sender,
                receiver,
                sticker,
                message
        );

        StickerShare saved =
                stickerShareRepository.saveAndFlush(gift);

        // TODO: 선물 수신 알림 생성
        // 알림과 선물 ID 연결 방식 확정 후 연동

        // 10. 응답 반환
        return new StickerGiftCreateResponse(
                saved.getId(),
                sticker.getId(),
                receiver.getId(),
                saved.getMessage(),
                saved.getStatus(),
                saved.getCreatedAt()
        );
    }

    /**
     * 스티커 선물 수락 또는 거절
     */
    @Transactional
    public StickerGiftUpdateResponse updateGift(
            Integer giftId,
            StickerGiftUpdateRequest request
    ) {

        User currentUser = currentUserService.get();

        // 1. 허용된 상태인지 확인
        if (request.status() != StickerShareStatus.ACCEPTED
                && request.status() != StickerShareStatus.REJECTED) {

            throw new ApiException(
                    ErrorCode.VALIDATION_FAILED,
                    "수락 또는 거절만 가능합니다."
            );
        }

        // 2. 선물 요청 조회 및 잠금
        StickerShare gift = stickerShareRepository
                .findByIdForUpdate(giftId)
                .orElseThrow(() -> new ApiException(
                        ErrorCode.NOT_FOUND,
                        "해당 선물 요청을 찾을 수 없습니다."
                ));

        // 3. 수신자 본인인지 확인
        if (!gift.getReceiver().getId()
                .equals(currentUser.getId())) {

            throw new ApiException(
                    ErrorCode.FORBIDDEN,
                    "해당 선물을 처리할 권한이 없습니다."
            );
        }

        // 4. 이미 처리된 선물인지 확인
        if (gift.getStatus() != StickerShareStatus.PENDING) {
            throw new ApiException(
                    ErrorCode.VALIDATION_FAILED,
                    "이미 처리된 선물입니다."
            );
        }

        Integer receivedStickerId = null;

        // 5. 수락 또는 거절 처리
        if (request.status() == StickerShareStatus.ACCEPTED) {

            // 원본 유지, 수신자에게 새 스티커 생성
            receivedStickerId =
                    stickerTransferService.copySticker(
                            gift.getSticker(),
                            currentUser
                    );

            gift.accept();

        } else {

            // 거절 시 스티커를 생성하지 않음
            gift.reject();
        }

        // 6. 상태 변경 저장
        StickerShare saved =
                stickerShareRepository.saveAndFlush(gift);

        // 선물 알림은 삭제하지 않음
        // 발신자에게 결과 알림을 보내지 않음

        // 7. 응답 반환
        return new StickerGiftUpdateResponse(
                saved.getId(),
                saved.getSender().getId(),
                saved.getSticker().getId(),
                receivedStickerId,
                saved.getStatus(),
                saved.getUpdatedAt()
        );
    }
}