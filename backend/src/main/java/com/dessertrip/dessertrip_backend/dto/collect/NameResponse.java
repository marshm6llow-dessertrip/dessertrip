package com.dessertrip.dessertrip_backend.dto.collect;

/** 카테고리/태그 추가 결과. 이미 있으면 기존 이름이 그대로 돌아온다. */
public record NameResponse(String name) { }
