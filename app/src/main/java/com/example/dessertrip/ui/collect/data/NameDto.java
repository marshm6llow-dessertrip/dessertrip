package com.example.dessertrip.ui.collect.data;

/** 카테고리/태그 추가 요청 {"name": "..."} 와 응답에 공통으로 사용 */
public class NameDto {
    public String name;

    public NameDto() { }
    public NameDto(String name) { this.name = name; }
}
