package com.dessertrip.dessertrip_backend.config.collect;

import com.dessertrip.dessertrip_backend.entity.collect.Dessert;
import com.dessertrip.dessertrip_backend.entity.collect.DessertCategory;
import com.dessertrip.dessertrip_backend.entity.collect.Sticker;
import com.dessertrip.dessertrip_backend.entity.collect.User;
import com.dessertrip.dessertrip_backend.repository.collect.CategoryRepository;
import com.dessertrip.dessertrip_backend.repository.collect.DessertRepository;
import com.dessertrip.dessertrip_backend.repository.collect.StickerRepository;
import com.dessertrip.dessertrip_backend.repository.collect.UserRepository;
import com.dessertrip.dessertrip_backend.service.collect.FileStorageService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

/**
 * [임시 · 개발용] 선물받은 스티커 기능을 테스트하기 위한 더미 데이터.
 * 첫 번째 사용자에게 "친구 스티커 1", "친구 스티커 2"(카테고리 "친구", 획득 방식 GIFT)를 넣는다.
 * 도감에는 나오지 않고 플레이트 선택 창(받은 스티커)에만 나온다. 이미 있으면 다시 만들지 않는다. 선물 기능이 완성되면 이 파일을 지우면 된다
 * (이미 들어간 더미는 sticker / dessert 에서 이름이 "친구 스티커"로 시작하는 행을 지우면 된다).
 */
@Component
@Profile("local")
@Order(2)
public class FriendStickerSeeder implements ApplicationRunner {

    private static final String CATEGORY = "친구";
    private static final String[] NAMES = {"친구 스티커 1", "친구 스티커 2"};
    private static final String[] FILES = {"f1e2d3c4-0000-4000-8000-000000000001.png", "f1e2d3c4-0000-4000-8000-000000000002.png"};
    private static final Color[] COLORS = {new Color(0x22BBD3), new Color(0x7C8CF8)};

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final StickerRepository stickerRepository;
    private final DessertRepository dessertRepository;
    private final FileStorageService fileStorage;

    public FriendStickerSeeder(UserRepository userRepository, CategoryRepository categoryRepository,
                               StickerRepository stickerRepository, DessertRepository dessertRepository,
                               FileStorageService fileStorage) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.stickerRepository = stickerRepository;
        this.dessertRepository = dessertRepository;
        this.fileStorage = fileStorage;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        User me = userRepository.findFirstByOrderByIdAsc().orElse(null);
        if (me == null) return;
        // "친구" 카테고리는 카테고리 추천·통계에 나오지 않게 숨겨 둔다(이미 있으면 숨김 처리)
        DessertCategory category = categoryRepository.findFirstByNameIgnoreCaseOrderByIdAsc(CATEGORY)
                .orElseGet(() -> categoryRepository.save(new DessertCategory(CATEGORY, 99)));
        category.deactivate();
        List<Dessert> mine = dessertRepository.findByUserIdOrderByIdAsc(me.getId());
        for (int i = 0; i < NAMES.length; i++) {
            final String name = NAMES[i];
            if (mine.stream().anyMatch(d -> name.equals(d.getName()))) continue;
            fileStorage.putIfAbsent(FILES[i], circlePng(COLORS[i]));
            Sticker sticker = stickerRepository.save(Sticker.gift(me.getId(), name, FILES[i]));
            Dessert dessert = new Dessert(me.getId(), sticker, name);
            dessert.updateBasics(name, category);
            dessertRepository.save(dessert);
        }
    }

    /** 투명 배경에 하얀 테두리가 있는 색 원 (글꼴이 없는 서버에서도 그려지도록 글자는 쓰지 않는다) */
    private static byte[] circlePng(Color color) {
        BufferedImage img = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillOval(8, 8, 240, 240);
        g.setColor(color);
        g.fillOval(24, 24, 208, 208);
        g.dispose();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(img, "png", out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
