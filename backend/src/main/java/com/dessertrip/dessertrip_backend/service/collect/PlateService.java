package com.dessertrip.dessertrip_backend.service.collect;

import com.dessertrip.dessertrip_backend.common.ApiException;
import com.dessertrip.dessertrip_backend.common.ErrorCode;
import com.dessertrip.dessertrip_backend.common.collect.FileUrlResolver;
import com.dessertrip.dessertrip_backend.common.collect.PlateLayout;
import com.dessertrip.dessertrip_backend.common.collect.PlatePolicy;
import com.dessertrip.dessertrip_backend.dto.collect.PlateResponse;
import com.dessertrip.dessertrip_backend.dto.collect.SavePlateRequest;
import com.dessertrip.dessertrip_backend.entity.collect.DessertPlate;
import com.dessertrip.dessertrip_backend.entity.collect.PlateSticker;
import com.dessertrip.dessertrip_backend.entity.collect.Sticker;
import com.dessertrip.dessertrip_backend.entity.collect.User;
import com.dessertrip.dessertrip_backend.repository.collect.DessertPlateRepository;
import com.dessertrip.dessertrip_backend.repository.collect.PlateStickerRepository;
import com.dessertrip.dessertrip_backend.repository.collect.StickerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * 플레이트(접시): 조회와 저장. 저장은 보낸 sticker_ids 로 기존 구성을 통째로 교체한다.
 * 위치는 서버가 무작위로 배치한다: 스티커를 만들면 자동으로 접시에 올라가고(addNewSticker),
 * 플레이트를 조회할 때마다 모든 스티커의 위치를 새로 섞어서 저장하고 돌려준다.
 * sticker_id 는 sticker 테이블의 id 다. TODO: 친구에게 받은 스티커(선물)는 선물 기능이 생긴 뒤에 추가.
 */
@Service
public class PlateService {

    private static final Random RANDOM = new Random();

    private final StickerRepository stickerRepository;
    private final DessertPlateRepository plateRepository;
    private final PlateStickerRepository plateStickerRepository;
    private final CurrentUserService currentUser;
    private final FileUrlResolver urls;

    public PlateService(StickerRepository stickerRepository, DessertPlateRepository plateRepository,
                        PlateStickerRepository plateStickerRepository, CurrentUserService currentUser,
                        FileUrlResolver urls) {
        this.stickerRepository = stickerRepository;
        this.plateRepository = plateRepository;
        this.plateStickerRepository = plateStickerRepository;
        this.currentUser = currentUser;
        this.urls = urls;
    }

    /**
     * 내 플레이트 조회. 조회할 때마다 위치를 새로 무작위로 정해 저장한다.
     * 플레이트가 아직 없으면 이때 빈 플레이트를 만든다(문서의 "플레이트 생성 시점"은 확인 필요 항목).
     */
    @Transactional
    public PlateResponse getPlate() {
        User me = currentUser.get();
        DessertPlate plate = plateOf(me);
        List<PlateSticker> rows = new ArrayList<>();
        List<Sticker> stickers = new ArrayList<>();
        for (PlateSticker ps : plateStickerRepository.findByPlateIdOrderByDisplayOrderAsc(plate.getId())) {
            Sticker sticker = stickerRepository.findById(ps.getStickerId()).orElse(null);
            if (sticker == null) continue;   // 삭제된 스티커는 건너뛴다
            rows.add(ps);
            stickers.add(sticker);
        }
        List<double[]> spots = PlateLayout.random(rows.size(), RANDOM);
        List<PlateResponse.Item> items = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).moveTo(spots.get(i)[0], spots.get(i)[1]);   // 트랜잭션이 끝나면 저장된다
            items.add(new PlateResponse.Item(rows.get(i).getStickerId(), urls.toUrl(stickers.get(i).getImgUrl()),
                    spots.get(i)[0], spots.get(i)[1]));
        }
        return new PlateResponse(plate.getId(), items);
    }

    /**
     * 새 스티커를 만들었을 때 접시에 자동으로 올린다. 접시가 가득 찼으면(최대 개수) 올리지 않고 넘어간다.
     * 위치는 접시를 조회할 때 새로 정해지므로 여기서는 가운데 근처의 임시 위치만 넣는다.
     */
    @Transactional
    public void addNewSticker(User me, int stickerId) {
        DessertPlate plate = plateOf(me);
        int count = plateStickerRepository.findByPlateIdOrderByDisplayOrderAsc(plate.getId()).size();
        if (count >= PlatePolicy.MAX_PLATE_STICKERS) return;
        double[] spot = PlateLayout.random(1, RANDOM).get(0);
        plateStickerRepository.save(new PlateSticker(plate.getId(), stickerId, spot[0], spot[1], count));
    }

    /** 접시 위 스티커를 보낸 목록으로 통째로 교체한다. 하나라도 잘못되면 아무것도 바꾸지 않는다. */
    @Transactional
    public PlateResponse savePlacements(SavePlateRequest request) {
        User me = currentUser.get();
        List<Long> ids = request.stickerIds();
        if (ids.size() > PlatePolicy.MAX_PLATE_STICKERS) throw new ApiException(ErrorCode.PLATE_FULL,
                "접시에는 스티커를 최대 " + PlatePolicy.MAX_PLATE_STICKERS + "개까지 올릴 수 있어요.");

        Set<Long> seen = new HashSet<>();
        for (Long id : ids) {
            if (id == null) throw ApiException.ofField(ErrorCode.VALIDATION_FAILED, "sticker_ids", "빈 값이 있습니다.");
            if (!seen.add(id)) {
                throw ApiException.ofField(ErrorCode.VALIDATION_FAILED, "sticker_ids", "같은 스티커를 두 번 올릴 수 없습니다.");
            }
            boolean mine = id >= 1 && id <= Integer.MAX_VALUE && stickerRepository.existsByIdAndUserId(id.intValue(), me.getId());
            if (!mine) throw new ApiException(ErrorCode.STICKER_NOT_FOUND, "스티커(" + id + ")를 찾을 수 없습니다.");
        }

        DessertPlate plate = plateOf(me);
        plateStickerRepository.deleteByPlateId(plate.getId());
        List<double[]> spots = PlateLayout.random(ids.size(), RANDOM);
        List<PlateSticker> rows = new ArrayList<>();
        List<PlateResponse.Item> items = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            double x = spots.get(i)[0], y = spots.get(i)[1];
            rows.add(new PlateSticker(plate.getId(), ids.get(i).intValue(), x, y, i));
            items.add(new PlateResponse.Item(ids.get(i).intValue(), null, x, y));
        }
        plateStickerRepository.saveAll(rows);
        return new PlateResponse(plate.getId(), items);
    }

    private DessertPlate plateOf(User me) {
        return plateRepository.findFirstByUserIdOrderByPlateOrderAsc(me.getId())
                .orElseGet(() -> plateRepository.save(new DessertPlate(me.getId(), 1)));
    }
}
