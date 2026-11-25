package com.dessertrip.dessertrip_backend.service.collect;

import com.dessertrip.dessertrip_backend.entity.collect.Tag;
import com.dessertrip.dessertrip_backend.repository.collect.TagRepository;
import com.dessertrip.dessertrip_backend.dto.collect.NameResult;
import com.dessertrip.dessertrip_backend.common.collect.NameValidator;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TagService {

    private static final int DEFAULT_LIST_SIZE = 30;

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Transactional(readOnly = true)
    public List<String> search(String query) {
        String q = query == null ? "" : query.trim();
        List<Tag> found = q.isEmpty()
                ? tagRepository.findAllByOrderByIdAsc(PageRequest.of(0, DEFAULT_LIST_SIZE))
                : tagRepository.findByNameContainingIgnoreCaseOrderByIdAsc(q);
        return found.stream().map(Tag::getName).toList();
    }

    /** 없으면 추가, 있으면(대소문자 무시) 기존 이름 반환 */
    @Transactional
    public NameResult addIfMissing(String rawName) {
        String name = NameValidator.clean(rawName, "name");
        return tagRepository.findFirstByNameIgnoreCaseOrderByIdAsc(name)
                .map(t -> new NameResult(t.getName(), false))
                .orElseGet(() -> new NameResult(tagRepository.save(new Tag(name)).getName(), true));
    }

    /** 테이스팅 노트 저장 시 새 태그도 태그 목록에 함께 저장하기 위한 내부용 (호출하는 쪽 트랜잭션에 참여) */
    @Transactional
    public Tag findOrCreate(String rawName, String field) {
        String name = NameValidator.clean(rawName, field);
        return tagRepository.findFirstByNameIgnoreCaseOrderByIdAsc(name).orElseGet(() -> tagRepository.save(new Tag(name)));
    }
}
