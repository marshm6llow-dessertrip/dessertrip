package com.dessertrip.dessertrip_backend.controller.collect;

import com.dessertrip.dessertrip_backend.service.collect.CategoryService;
import com.dessertrip.dessertrip_backend.dto.collect.NameRequest;
import com.dessertrip.dessertrip_backend.dto.collect.NameResponse;
import com.dessertrip.dessertrip_backend.dto.collect.NameResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<String> search(@RequestParam(required = false) String query) {
        return categoryService.search(query);
    }

    /** 새로 추가하면 201, 이미 있으면 200 + 기존 이름 */
    @PostMapping
    public ResponseEntity<NameResponse> add(@RequestBody NameRequest request) {
        NameResult result = categoryService.addIfMissing(request.name());
        return ResponseEntity.status(result.created() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(new NameResponse(result.name()));
    }
}
