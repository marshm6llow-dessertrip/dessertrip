package com.dessertrip.dessertrip_backend.dto.collect;

/** 본문 없이 결과 메시지만 돌려주는 응답 (예: 테이스팅 노트 삭제) */
public record MessageResponse(String message) { }
