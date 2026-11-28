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
}