package com.dessertrip.dessertrip_backend.dto.collect;

/** 이름 추가 결과: created=true 면 새로 저장(201), false 면 이미 있던 것(200) */
public record NameResult(String name, boolean created) { }
