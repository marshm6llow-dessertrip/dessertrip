package com.dessertrip.dessertrip_backend.service.collect;

import com.dessertrip.dessertrip_backend.common.ApiException;
import com.dessertrip.dessertrip_backend.common.ErrorCode;
import com.dessertrip.dessertrip_backend.dto.collect.CafeListResponse;
import com.dessertrip.dessertrip_backend.entity.collect.Cafe;
import com.dessertrip.dessertrip_backend.repository.collect.CafeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 카페 목록 조회. 지금 지원하는 조건: keyword(이름/주소), bounds. 나머지(category_id, 가격, 태그, zoom)는 받지만 아직 적용하지 않는다.
 * bounds 형식은 문서에 정해져 있지 않아 "남서위도,남서경도,북동위도,북동경도" 로 가정했다 (팀 확인 필요).
 */
@Service
public class PlaceService {

    private final CafeRepository cafeRepository;

    public PlaceService(CafeRepository cafeRepository) {
        this.cafeRepository = cafeRepository;
    }

    @Transactional(readOnly = true)
    public CafeListResponse search(String keyword, String bounds) {
        double[] box = parseBounds(bounds);
        String q = keyword == null ? "" : keyword.trim();
        List<Cafe> found = q.isEmpty() ? cafeRepository.findAll()
                : cafeRepository.findByNameContainingIgnoreCaseOrAddressContainingIgnoreCase(q, q);
        List<CafeListResponse.Item> items = found.stream()
                .filter(c -> box == null || (c.getLatitude() >= box[0] && c.getLatitude() <= box[2]
                        && c.getLongitude() >= box[1] && c.getLongitude() <= box[3]))
                .map(CafeListResponse.Item::from).toList();
        return new CafeListResponse(items, items.size());
    }

    private static double[] parseBounds(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String[] p = raw.split(",");
        try {
            if (p.length != 4) throw new NumberFormatException();
            double[] v = new double[4];
            for (int i = 0; i < 4; i++) v[i] = Double.parseDouble(p[i].trim());
            if (v[0] > v[2] || v[1] > v[3]) throw new NumberFormatException();
            return v;
        } catch (NumberFormatException e) {
            throw ApiException.ofField(ErrorCode.VALIDATION_FAILED, "bounds", "남서위도,남서경도,북동위도,북동경도 형식이어야 합니다.");
        }
    }
}
