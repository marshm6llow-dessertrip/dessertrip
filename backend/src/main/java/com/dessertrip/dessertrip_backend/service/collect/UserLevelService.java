package com.dessertrip.dessertrip_backend.service.collect;

import com.dessertrip.dessertrip_backend.entity.collect.User;
import com.dessertrip.dessertrip_backend.entity.collect.UserTitle;
import com.dessertrip.dessertrip_backend.repository.collect.UserTitleRepository;
import com.dessertrip.dessertrip_backend.common.collect.PlatePolicy;
import com.dessertrip.dessertrip_backend.repository.collect.DessertRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 도감 개수에 따른 레벨/칭호. 칭호 이름은 user_title 테이블에서 읽고, 없으면 코드의 기본 이름을 쓴다. */
@Service
public class UserLevelService {

    private final DessertRepository dessertRepository;
    private final UserTitleRepository userTitleRepository;

    public UserLevelService(DessertRepository dessertRepository, UserTitleRepository userTitleRepository) {
        this.dessertRepository = dessertRepository;
        this.userTitleRepository = userTitleRepository;
    }

    @Transactional(readOnly = true)
    public String titleNameFor(int level) {
        List<UserTitle> found = userTitleRepository.findForLevel(level, PageRequest.of(0, 1));
        return found.isEmpty() ? PlatePolicy.LEVEL_TITLES.get(level - 1) : found.get(0).getName();
    }

    /** 디저트가 늘거나 줄었을 때 user 의 레벨/칭호를 맞춘다 (호출하는 쪽 트랜잭션에 참여) */
    @Transactional
    public void sync(User user) {
        int level = PlatePolicy.levelFor(dessertRepository.countByUserIdAndStickerPresentFalse(user.getId()));
        List<UserTitle> found = userTitleRepository.findForLevel(level, PageRequest.of(0, 1));
        user.updateLevel(level, found.isEmpty() ? null : found.get(0));
    }
}
