package com.dessertrip.dessertrip_backend.common.collect;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** 저장된 파일 이름 <-> 앱이 받는 이미지 URL 변환 (요청이 들어온 서버 주소 기준) */
@Component
public class FileUrlResolver {

    private static final String PATH = "/api/files/";

    public String toUrl(String fileName) {
        if (fileName == null) return null;
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(PATH).path(fileName).toUriString();
    }

    /** 앱이 보내온 URL 에서 파일 이름만 꺼낸다. 우리 경로가 아니면 null. */
    public String toFileName(String url) {
        if (url == null) return null;
        int i = url.indexOf(PATH);
        return i < 0 ? null : url.substring(i + PATH.length());
    }
}
