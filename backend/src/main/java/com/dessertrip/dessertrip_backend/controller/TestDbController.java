package com.dessertrip.dessertrip_backend.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test-db")
public class TestDbController {

    private final JdbcTemplate jdbcTemplate;

    public TestDbController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // 1. 데이터 저장하기
    @PostMapping
    public Map<String, String> saveMessage(
            @RequestBody Map<String, String> request
    ) {
        String message = request.get("message");

        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message는 필수입니다.");
        }

        String sql = """
                INSERT INTO test_messages (message)
                VALUES (?)
                """;

        jdbcTemplate.update(sql, message);

        return Map.of(
                "status", "success",
                "message", "데이터 저장 성공"
        );
    }

    // 2. 저장된 데이터 전체 조회하기
    @GetMapping
    public List<Map<String, Object>> getMessages() {

        String sql = """
                SELECT id, message, created_at
                FROM test_messages
                ORDER BY id DESC
                """;

        return jdbcTemplate.queryForList(sql);
    }
}
