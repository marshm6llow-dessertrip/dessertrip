package com.dessertrip.dessertrip_backend.controller.collect;

import com.dessertrip.dessertrip_backend.dto.collect.BasicsRequest;
import com.dessertrip.dessertrip_backend.dto.collect.MessageResponse;
import com.dessertrip.dessertrip_backend.dto.collect.NoteDetailResponse;
import com.dessertrip.dessertrip_backend.dto.collect.NoteUpdateResponse;
import com.dessertrip.dessertrip_backend.dto.collect.StickerCreatedResponse;
import com.dessertrip.dessertrip_backend.dto.collect.TastingNoteRequest;
import com.dessertrip.dessertrip_backend.service.collect.DessertService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/** 스티커 생성 / 테이스팅 노트 API — 경로와 본문은 팀 API 문서(/api/v1/...) 기준 */
@RestController
@RequestMapping("/api")
public class DessertController {

    private final DessertService dessertService;

    public DessertController(DessertService dessertService) {
        this.dessertService = dessertService;
    }

    /** 테이스팅 노트 상세 조회 */
    @GetMapping("/v1/users/me/tasting-notes/{noteId}")
    public NoteDetailResponse get(@PathVariable long noteId) {
        return dessertService.get(noteId);
    }

    /**
     * 디저트 스티커 생성 및 저장 — multipart: image (필수), dessert_id (문서에는 있으나 지금은 받지 않는다:
     * 스티커를 만들 때 dessert 행을 함께 만들기로 했기 때문. 보내도 무시된다.)
     */
    @PostMapping(value = "/v1/users/me/stickers", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StickerCreatedResponse> createSticker(@RequestPart("image") MultipartFile image) {
        StickerCreatedResponse created = dessertService.createSticker(image);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/users/me/tasting-notes/{id}").buildAndExpand(created.dessertId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    /** 테이스팅 노트 수정 — JSON 본문 (문서 형식) */
    @PatchMapping(value = "/v1/users/me/tasting-notes/{noteId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public NoteUpdateResponse updateNote(@PathVariable long noteId, @Valid @RequestBody TastingNoteRequest request) {
        return dessertService.updateNote(noteId, request, List.of());
    }

    /** 테이스팅 노트 수정 — 사진을 함께 올릴 때: multipart 의 request(JSON) + photos[] */
    @PatchMapping(value = "/v1/users/me/tasting-notes/{noteId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public NoteUpdateResponse updateNoteWithPhotos(@PathVariable long noteId,
                                                   @Valid @RequestPart("request") TastingNoteRequest request,
                                                   @RequestPart(value = "photos", required = false) List<MultipartFile> photos) {
        return dessertService.updateNote(noteId, request, photos);
    }

    /** 테이스팅 노트 삭제 — 노트 내용만 지우고 스티커는 남긴다 */
    @DeleteMapping("/v1/users/me/tasting-notes/{noteId}")
    public MessageResponse deleteNote(@PathVariable long noteId) {
        dessertService.deleteNote(noteId);
        return new MessageResponse("테이스팅 노트가 삭제되었습니다.");
    }

    /** (API 문서에 없음) 스티커 완성 직후 이름·카테고리 저장 */
    @PatchMapping("/desserts/{id}")
    public NoteDetailResponse updateBasics(@PathVariable long id, @Valid @RequestBody BasicsRequest request) {
        return dessertService.updateBasics(id, request);
    }

    /** (API 문서에 없음) 스티커 삭제 — 스티커와 테이스팅 노트를 모두 삭제. 노트만 지우는 문서의 DELETE .../tasting-notes/{noteId} 와 다르다 */
    @DeleteMapping("/desserts/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        dessertService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
