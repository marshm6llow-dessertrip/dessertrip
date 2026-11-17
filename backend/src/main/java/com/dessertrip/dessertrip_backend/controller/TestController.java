package com.dessertrip.dessertrip_backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class TestController {

    @GetMapping("/api/test")
    public Map<String, String> testCheck() {
        return Map.of(
                "status", "UP",
                "message", "Dessertrip server is running"
        );
    }
}
