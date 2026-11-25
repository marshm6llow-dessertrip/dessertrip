package com.example.dessertrip.ui.friend.data;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

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
}