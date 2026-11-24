package com.dessertrip.dessertrip_backend.controller.collect;

import com.dessertrip.dessertrip_backend.service.collect.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/** 업로드된 이미지(스티커·사진) 내려주기. 이미지는 Glide 가 헤더 없이 요청하므로 개발 단계에서는 공개한다. */
@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileStorageService fileStorage;

    public FileController(FileStorageService fileStorage) {
        this.fileStorage = fileStorage;
    }

    // TODO: 인증 방식 확정 후 이미지 접근 제어(서명 URL 등) 여부 결정
    @GetMapping("/{fileName:.+}")
    public ResponseEntity<Resource> get(@PathVariable String fileName) {
        Resource resource = fileStorage.load(fileName);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileStorage.contentType(fileName)))
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
                .body(resource);
    }
}
