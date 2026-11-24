package com.dessertrip.dessertrip_backend.service.collect;

import com.dessertrip.dessertrip_backend.entity.collect.User;
import com.dessertrip.dessertrip_backend.repository.collect.UserRepository;
import com.dessertrip.dessertrip_backend.common.ApiException;
import com.dessertrip.dessertrip_backend.common.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * "지금 요청한 사용자". 로그인(JWT)이 연결되기 전까지는 user 테이블의 첫 번째 유저를 쓴다.
 * TODO: 인증 담당자가 JWT 를 연결하면 토큰에 든 사용자로 바꾼다. (이 클래스 한 곳만 고치면 된다)
 */
@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public User get() {
        return userRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED, "사용자 정보가 없습니다. user 테이블에 사용자를 먼저 등록해 주세요."));
    }
}
