package com.example.dessertrip.ui.collect.data;

/** 스티커 완성 직후 필수 정보(이름, 카테고리) 저장 요청: PATCH /archive/{id} */
public class BasicsRequest {
    public String dessertName;
    public String category;

    public BasicsRequest(String dessertName, String category) {
        this.dessertName = dessertName;
        this.category = category;
    }
}
