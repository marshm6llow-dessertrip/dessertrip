package com.example.dessertrip.ui.friend.data;

import com.example.dessertrip.ui.collect.core.network.ApiCallback;
import com.example.dessertrip.ui.collect.core.network.ApiErrorParser;

import retrofit2.Call;

public class FriendRepository {

    private final FriendApi api;
    private final ApiErrorParser parser;

    public FriendRepository(FriendApi api, ApiErrorParser parser) {
        this.api = api;
        this.parser = parser;
    }

    // 친구 코드로 사용자 검색
    public Call<FriendCodeSearchDto> searchByFriendCode(
            String friendCode,
            ApiCallback<FriendCodeSearchDto> callback
    ) {
        return parser.enqueue(
                api.searchByFriendCode(friendCode),
                callback
        );
    }

    // 친구 요청 보내기
    public Call<FriendRequestResponseDto> sendFriendRequest(
            int receiverId,
            ApiCallback<FriendRequestResponseDto> callback
    ) {
        FriendRequestDto request = new FriendRequestDto(receiverId);

        return parser.enqueue(
                api.sendFriendRequest(request),
                callback
        );
    }

    // 타인 공개 플레이트 조회
    public Call<PublicPlateDto> getPublicPlate(
            int userId,
            Integer categoryId,
            ApiCallback<PublicPlateDto> callback
    ) {
        return parser.enqueue(
                api.getPublicPlate(userId, categoryId),
                callback
        );
    }

    // 친구 목록 조회 및 닉네임 검색
    public Call<FriendListDto> getFriends(
            String keyword,
            ApiCallback<FriendListDto> callback
    ) {
        return parser.enqueue(
                api.getFriends(keyword),
                callback
        );
    }

    // 스티커 선물 보내기
    public Call<StickerGiftCreateResponse> sendStickerGift(
            int stickerId,
            int receiverId,
            String message,
            ApiCallback<StickerGiftCreateResponse> callback
    ) {
        StickerGiftCreateRequest request =
                new StickerGiftCreateRequest(
                        stickerId,
                        receiverId,
                        message
                );

        return parser.enqueue(
                api.sendStickerGift(request),
                callback
        );
    }
}