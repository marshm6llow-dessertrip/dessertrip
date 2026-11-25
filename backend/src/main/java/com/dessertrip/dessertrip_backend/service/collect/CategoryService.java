package com.dessertrip.dessertrip_backend.service.collect;

import com.dessertrip.dessertrip_backend.entity.collect.DessertCategory;
import com.dessertrip.dessertrip_backend.repository.collect.CategoryRepository;
import com.dessertrip.dessertrip_backend.dto.collect.NameResult;
import com.dessertrip.dessertrip_backend.common.collect.NameValidator;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<String> search(String query) {
        String q = query == null ? "" : query.trim();
        List<DessertCategory> found = q.isEmpty()
                ? categoryRepository.findByActiveTrueOrderByDisplayOrderAscIdAsc()
                : categoryRepository.findByActiveTrueAndNameContainingIgnoreCaseOrderByDisplayOrderAscIdAsc(q);
        return found.stream().map(DessertCategory::getName).toList();
    }

    /** 없으면 추가, 있으면(대소문자 무시) 기존 이름 반환 */
    @Transactional
    public NameResult addIfMissing(String rawName) {
        String name = NameValidator.clean(rawName, "name");
        return categoryRepository.findFirstByNameIgnoreCaseOrderByIdAsc(name)
                .map(c -> new NameResult(c.getName(), false))
                .orElseGet(() -> new NameResult(
                        categoryRepository.save(new DessertCategory(name, (int) categoryRepository.count() + 1)).getName(), true));
    }
}
