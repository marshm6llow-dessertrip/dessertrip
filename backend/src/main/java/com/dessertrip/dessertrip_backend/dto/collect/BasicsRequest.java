package com.dessertrip.dessertrip_backend.dto.collect;

import com.dessertrip.dessertrip_backend.common.collect.NamePolicy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 스티커 완성 직후 필수 정보(이름, 카테고리) 저장 요청: PATCH /api/desserts/{id} */
public record BasicsRequest(
        @NotBlank(message = "필수 입력값입니다.")
        @Size(max = NamePolicy.MAX_DESSERT_NAME_LENGTH, message = "50자 이하여야 합니다.")
        String dessertName,

        @NotBlank(message = "필수 입력값입니다.")
        String category) { }
