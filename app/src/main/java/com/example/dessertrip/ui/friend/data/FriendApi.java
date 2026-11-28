package com.example.dessertrip.ui.friend.data;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Path;
public interface FriendApi {

    // 친구 코드로 사용자 검색
    @GET("api/v1/users/search-by-code")
    Call<FriendCodeSearchDto> searchByFriendCode(
            @Query("friend_code") String friendCode
    );

    // 친구 요청 보내기
    @POST("api/v1/users/me/friend-requests")
    Call<FriendRequestResponseDto> sendFriendRequest(
            @Body FriendRequestDto request
    );

    // 타인 공개 플레이트 조회
    @GET("api/v1/users/{userId}/plate")
    Call<PublicPlateDto> getPublicPlate(
            @Path("userId") int userId,
            @Query("category_id") Integer categoryId
    );

    // 친구 목록 조회 및 닉네임 검색
    @GET("api/v1/users/me/friends")
    Call<FriendListDto> getFriends(
            @Query("keyword") String keyword
    );

    // 스티커 선물 보내기
    @POST("api/v1/users/me/sticker-gifts")
    Call<StickerGiftCreateResponse> sendStickerGift(
            @Body StickerGiftCreateRequest request
    );
}