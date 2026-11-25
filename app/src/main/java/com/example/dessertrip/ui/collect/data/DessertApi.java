package com.example.dessertrip.ui.collect.data;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** 기능별 API. 경로와 본문은 팀 API 문서(/api/v1/...) 기준이고, 문서에 없는 것은 주석에 표시했다. */
public interface DessertApi {

    // ---- 내 도감 / 프로필 ----
    @GET("api/v1/users/me/collection")  // 200
    Call<CollectionDto> getCollection(@Query("category_id") Integer categoryId, @Query("sort") String sort,
                                      @Query("limit") Integer limit, @Query("keyword") String keyword);

    @GET("api/v1/users/me/collection/stats")  // 200
    Call<StatsDto> getCollectionStats();

    @GET("api/v1/users/me/profile")  // 200
    Call<ProfileDto> getProfile();

    // ---- 플레이트 ----
    @GET("api/v1/users/me/plate")  // 200
    Call<PlateResponseDto> getPlate();

    @PUT("api/v1/users/me/plate/stickers")  // 200, body: {sticker_ids}
    Call<PlateResponseDto> savePlateStickers(@Body SavePlateRequest body);

    // ---- 스티커 / 테이스팅 노트 ----
    @Multipart
    @POST("api/v1/users/me/stickers")  // 201, multipart: image
    Call<StickerCreatedDto> createSticker(@Part MultipartBody.Part image);

    @GET("api/v1/users/me/tasting-notes/{noteId}")  // 200
    Call<NoteDetailDto> getNote(@Path("noteId") long noteId);

    @Multipart
    @PATCH("api/v1/users/me/tasting-notes/{noteId}")  // 200, multipart: request(JSON) + photos[]
    Call<NoteUpdateDto> saveNote(
            @Path("noteId") long noteId,
            @Part("request") RequestBody request,
            @Part List<MultipartBody.Part> photos);

    @DELETE("api/v1/users/me/tasting-notes/{noteId}")  // 200 {message} — 노트 내용만 삭제, 스티커는 남는다
    Call<MessageDto> deleteNote(@Path("noteId") long noteId);

    // ---- 카페 ----
    @GET("api/v1/cafes")  // 200
    Call<CafeListDto> getCafes(@Query("keyword") String keyword);

    // ---- 문서에 없는 API ----
    @DELETE("api/desserts/{id}")  // 204 — 스티커 삭제: 스티커와 테이스팅 노트, 접시 위 배치까지 함께 삭제
    Call<Void> deleteArchiveItem(@Path("id") long id);

    @PATCH("api/desserts/{id}")  // 200, body: {dessertName, category} — 스티커 완성 직후 필수 정보 저장
    Call<NoteDetailDto> updateBasics(@Path("id") long id, @Body BasicsRequest body);

    @GET("api/tags")  // 200
    Call<List<String>> getTags(@Query("query") String query);

    @POST("api/tags")  // 201(새로 추가) | 200(이미 있음), body: {name}
    Call<NameDto> createTag(@Body NameDto body);

    @GET("api/categories")  // 200
    Call<List<String>> getCategories(@Query("query") String query);

    @POST("api/categories")  // 201(새로 추가) | 200(이미 있음), body: {name}
    Call<NameDto> createCategory(@Body NameDto body);
}
