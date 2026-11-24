package com.dessertrip.dessertrip_backend.config.collect;

import com.dessertrip.dessertrip_backend.common.collect.PlatePolicy;
import com.dessertrip.dessertrip_backend.entity.collect.Cafe;
import com.dessertrip.dessertrip_backend.entity.collect.DessertCategory;
import com.dessertrip.dessertrip_backend.entity.collect.PostCategory;
import com.dessertrip.dessertrip_backend.entity.collect.Tag;
import com.dessertrip.dessertrip_backend.entity.collect.User;
import com.dessertrip.dessertrip_backend.entity.collect.UserTitle;
import com.dessertrip.dessertrip_backend.repository.collect.CafeRepository;
import com.dessertrip.dessertrip_backend.repository.collect.CategoryRepository;
import com.dessertrip.dessertrip_backend.repository.collect.PostCategoryRepository;
import com.dessertrip.dessertrip_backend.repository.collect.TagRepository;
import com.dessertrip.dessertrip_backend.repository.collect.UserRepository;
import com.dessertrip.dessertrip_backend.repository.collect.UserTitleRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 개발(local)에서 서버를 처음 켤 때 기본 데이터를 채운다 (비어 있는 테이블만).
 * 칭호, 카테고리, 태그, 게시글 종류, 샘플 카페, 개발용 사용자.
 * 개발용 사용자는 로그인용이 아니다: 비밀번호 칸에는 로그인할 수 없는 값("!")을 넣는다.
 */
@Component
@Profile("local")
@Order(1)   // 더미 스티커(FriendStickerSeeder)보다 먼저 실행
public class DataInitializer implements ApplicationRunner {

    private final UserTitleRepository userTitleRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final PostCategoryRepository postCategoryRepository;
    private final CafeRepository cafeRepository;

    public DataInitializer(UserTitleRepository userTitleRepository, UserRepository userRepository,
                           CategoryRepository categoryRepository, TagRepository tagRepository,
                           PostCategoryRepository postCategoryRepository, CafeRepository cafeRepository) {
        this.userTitleRepository = userTitleRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
        this.postCategoryRepository = postCategoryRepository;
        this.cafeRepository = cafeRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userTitleRepository.count() == 0) {
            List<String> titles = PlatePolicy.LEVEL_TITLES;
            for (int i = 0; i < titles.size(); i++) {
                int level = i + 1;
                boolean last = i == titles.size() - 1;
                userTitleRepository.save(new UserTitle(titles.get(i), level, last ? null : level));
            }
        }
        if (categoryRepository.count() == 0) {
            List<String> names = List.of("케이크", "빵", "구움과자");
            for (int i = 0; i < names.size(); i++) categoryRepository.save(new DessertCategory(names.get(i), i + 1));
        }
        if (tagRepository.count() == 0) {
            List.of("적당히 달아요", "고소해요", "상큼해요", "촉촉해요", "바삭해요", "크림이 부드러워요", "쫀득해요", "진해요")
                    .forEach(n -> tagRepository.save(new Tag(n)));
        }
        if (postCategoryRepository.count() == 0) {
            postCategoryRepository.save(new PostCategory("게시글", "유저 자유 게시글입니다."));
            postCategoryRepository.save(new PostCategory("리뷰", "카페/디저트 리뷰입니다."));
            postCategoryRepository.save(new PostCategory("테이스팅 노트", "테이스팅 노트를 커뮤니티에 공유한 글"));
        }
        if (cafeRepository.count() == 0) {
            cafeRepository.saveAll(List.of(
                    new Cafe("달콤한 과자점", "서울 마포구 와우산로 21길", 37.5547, 126.9236),
                    new Cafe("작은 오븐", "서울 성동구 서울숲길 12", 37.5446, 127.0447),
                    new Cafe("빵굽는 아침", "서울 용산구 이태원로 55", 37.5345, 126.9946),
                    new Cafe("디저트 연구소", "서울 강남구 논현로 150", 37.5172, 127.0286),
                    new Cafe("달빛 베이커리", "부산 해운대구 해운대해변로 264", 35.1587, 129.1604)));
        }
        if (userRepository.count() == 0) {
            UserTitle first = userTitleRepository.findAll().stream()
                    .min(java.util.Comparator.comparing(UserTitle::getMinLevel)).orElseThrow();
            // 이름/태그는 DB 에서 직접 바꿀 수 있다: UPDATE `user` SET user_name='내이름' WHERE id=1;
            userRepository.save(new User("Ka61234", "dev@dessertrip.local", "!", "디저트러버", first));
        }
    }
}
