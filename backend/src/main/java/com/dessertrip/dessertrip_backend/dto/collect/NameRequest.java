package com.dessertrip.dessertrip_backend.dto.collect;

/** 카테고리/태그 추가 요청 {"name": "..."} — 검증(공백 제거, 1~20자)은 서비스에서 한다. */
public record NameRequest(String name) { }
