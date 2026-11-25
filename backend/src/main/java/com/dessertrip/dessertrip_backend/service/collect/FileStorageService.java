package com.dessertrip.dessertrip_backend.service.collect;

import com.dessertrip.dessertrip_backend.common.collect.UploadPolicy;
import com.dessertrip.dessertrip_backend.common.ApiException;
import com.dessertrip.dessertrip_backend.common.ErrorCode;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 업로드 이미지 저장/조회/삭제. 파일 이름은 서버가 만든 UUID 라서 사용자 입력이 경로에 들어가지 않는다.
 * TODO: 운영에서는 로컬 디스크 대신 객체 스토리지(S3 등) 사용 여부를 팀과 결정
 */
@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);
    private static final Pattern SAFE_NAME = Pattern.compile("[0-9a-f\\-]{36}\\.(jpg|png|webp)");
    private static final Map<String, String> EXT = Map.of("image/jpeg", "jpg", "image/png", "png", "image/webp", "webp");
    private static final Map<String, String> TYPE_BY_EXT = Map.of("jpg", "image/jpeg", "png", "image/png", "webp", "image/webp");

    @Value("${app.upload-dir}")
    private String uploadDir;

    private Path root;

    @PostConstruct
    void init() throws IOException {
        root = Path.of(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(root);
    }

    /** 검증(형식·용량·실제 파일 내용) 후 저장하고 저장된 파일 이름을 돌려준다. */
    public String store(MultipartFile file, String field) {
        String type = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        if (!UploadPolicy.ALLOWED_IMAGE_TYPES.contains(type)) {
            throw ApiException.ofField(ErrorCode.INVALID_IMAGE_TYPE, field, "JPG, PNG, WEBP 형식만 업로드할 수 있습니다.");
        }
        if (file.getSize() > UploadPolicy.MAX_IMAGE_SIZE_BYTES) {
            throw ApiException.ofField(ErrorCode.IMAGE_TOO_LARGE, field,
                    "이미지는 " + (UploadPolicy.MAX_IMAGE_SIZE_BYTES / 1024 / 1024) + "MB 이하여야 합니다.");
        }
        try {
            byte[] bytes = file.getBytes();
            if (!matchesSignature(bytes, type)) {   // Content-Type 만 속인 파일 차단
                throw ApiException.ofField(ErrorCode.INVALID_IMAGE_TYPE, field, "이미지 파일이 아니거나 형식이 일치하지 않습니다.");
            }
            String name = UUID.randomUUID() + "." + EXT.get(type);
            Files.write(root.resolve(name), bytes);
            return name;
        } catch (IOException e) {
            throw new UncheckedIOException(e);   // 전역 핸들러가 500 으로 처리 (내부 정보 노출 없음)
        }
    }

    private boolean matchesSignature(byte[] b, String type) {
        if (b.length < 12) return false;
        return switch (type) {
            case "image/jpeg" -> (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8;
            case "image/png" -> (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G';
            case "image/webp" -> b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                    && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P';
            default -> false;
        };
    }

    /** 개발용 더미 데이터: 정해 둔 이름으로 파일을 저장한다(이미 있으면 그대로 둔다). 이름은 UUID 형식이어야 한다. */
    public void putIfAbsent(String fileName, byte[] bytes) {
        Path p = resolveSafe(fileName);
        if (p == null) throw new IllegalArgumentException("파일 이름 형식이 올바르지 않습니다.");
        try {
            if (!Files.exists(p)) Files.write(p, bytes);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Resource load(String fileName) {
        Path p = resolveSafe(fileName);
        if (p == null || !Files.isRegularFile(p)) throw new ApiException(ErrorCode.FILE_NOT_FOUND);
        return new PathResource(p);
    }

    public String contentType(String fileName) {
        return TYPE_BY_EXT.getOrDefault(fileName.substring(fileName.lastIndexOf('.') + 1), "application/octet-stream");
    }

    /** 삭제 실패는 요청 결과에 영향을 주지 않도록 로그만 남긴다. */
    public void deleteQuietly(String fileName) {
        try {
            Path p = resolveSafe(fileName);
            if (p != null) Files.deleteIfExists(p);
        } catch (IOException e) {
            log.warn("파일 삭제 실패: {}", fileName);
        }
    }

    private Path resolveSafe(String fileName) {
        if (fileName == null || !SAFE_NAME.matcher(fileName).matches()) return null;
        Path p = root.resolve(fileName).normalize();
        return p.startsWith(root) ? p : null;
    }
}
