-- ==========================================================================
-- 디저트립 테이블 생성 스크립트 (테이블 명세서 4차 수정본(2026.11.26) 기준, MySQL 8.x)
-- - 접속 계정/비밀번호는 이 파일에 적지 않는다. 계정은 팀 DB 안내 문서대로 만든다.
-- - 모든 테이블 공통: id(PK, AUTO_INCREMENT), created_at, updated_at, deleted_at(논리 삭제)
-- - DB 이름은 팀 설정 기본값(DB_NAME=dessertrip)에 맞췄다
-- - 논리 삭제(deleted_at)를 쓰는 테이블: 삭제해도 행이 남는다. 이메일/닉네임/친구 코드는 active_flag 와 함께 UNIQUE 라서 삭제 후 같은 값으로 다시 쓸 수 있다.
-- - 실제 삭제(DELETE)를 쓰는 테이블: plate_sticker, post_like, comment_like, post_mark, cafe_mark
-- - notification: 일반 알림은 논리 삭제(deleted_at)를 쓰지만, notification_type 이 'FRIEND_REQUEST' 인 알림은 친구 요청을 수락/거절/취소할 때 실제 삭제(DELETE)한다.
--   (거절한 뒤 같은 사용자가 다시 요청을 보냈을 때, 남아 있던 이전 알림이 새 PENDING 요청으로 이어지는 문제를 막기 위해서다)
--   알림 유형을 세분화해서(FRIEND_REQUEST / STICKER_REQUEST / STICKER_SHARE ...) 같은 보낸 사람의 다른 알림이 함께 지워지지 않게 한다.
-- ==========================================================================
CREATE DATABASE IF NOT EXISTS dessertrip CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE dessertrip;
SET NAMES utf8mb4;

-- 유저 칭호 종류
CREATE TABLE IF NOT EXISTS `user_title` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '데이터 고유 식별자',
    `title_name` VARCHAR(255) NOT NULL COMMENT '칭호 이름',
    `min_level` INT NOT NULL COMMENT '칭호가 부여되는 최소 레벨',
    `max_level` INT NULL COMMENT '칭호가 부여되는 최대 레벨',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 시각',
    `deleted_at` DATETIME NULL DEFAULT NULL COMMENT '삭제 시각(논리 삭제)',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='유저 칭호 종류';

-- 유저 정보 (이메일, 비밀번호, 이름, 소개글, 레벨, 칭호)
CREATE TABLE IF NOT EXISTS `user` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '데이터 고유 식별자',
    `user_friend_code` VARCHAR(255) NOT NULL COMMENT '친구추가 등에 사용될 유저 고유 태그',
    `user_email` VARCHAR(255) NOT NULL COMMENT '유저 이메일(로그인 아이디 대체?)',
    `user_password` VARCHAR(255) NOT NULL COMMENT '유저 패스워드 (해시값만 저장)',
    `user_name` VARCHAR(50) NOT NULL COMMENT '유저 닉네임',
    `user_description` VARCHAR(255) NULL COMMENT '유저 소개글',
    `user_level` INT NOT NULL DEFAULT 1 COMMENT '유저 레벨',
    `user_title_id` INT NOT NULL COMMENT '유저가 가진 칭호',
    `profile_img_url` VARCHAR(2048) NULL COMMENT '유저 프로필 이미지 주소',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 시각',
    `deleted_at` DATETIME NULL DEFAULT NULL COMMENT '삭제 시각(논리 삭제)',
    `active_flag` TINYINT GENERATED ALWAYS AS (IF(`deleted_at` IS NULL, 1, NULL)) VIRTUAL COMMENT '삭제 안 된 행이면 1, 삭제된 행이면 NULL (UNIQUE 키에 함께 넣어 삭제된 값은 다시 쓸 수 있게 함. 직접 입력하지 않는다)',
    PRIMARY KEY (`id`),
    UNIQUE KEY uq_user_friend_code (user_friend_code, active_flag),
    UNIQUE KEY uq_user_email (user_email, active_flag),
    UNIQUE KEY uq_user_name (user_name, active_flag),
    CONSTRAINT fk_user_title FOREIGN KEY (user_title_id) REFERENCES `user_title` (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='유저 정보 (이메일, 비밀번호, 이름, 소개글, 레벨, 칭호)';

-- 등록된 카페
CREATE TABLE IF NOT EXISTS `cafe` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '데이터 고유 식별자',
    `cafe_name` VARCHAR(50) NOT NULL COMMENT '카페 이름',
    `cafe_latitude` DECIMAL(10,7) NOT NULL COMMENT '카페 위도',
    `cafe_longitude` DECIMAL(10,7) NOT NULL COMMENT '카페 경도',
    `cafe_address` VARCHAR(255) NULL COMMENT '카페 주소',
    `cafe_email` VARCHAR(255) NULL COMMENT '카페 이메일',
    `cafe_call` VARCHAR(255) NULL COMMENT '카페 전화번호',
    `cafe_description` VARCHAR(255) NULL COMMENT '카페 설명',
    `cafe_img_url` VARCHAR(2048) NULL COMMENT '카페 이미지',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 시각',
    `deleted_at` DATETIME NULL DEFAULT NULL COMMENT '삭제 시각(논리 삭제)',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='등록된 카페';

-- 디저트/메뉴 카테고리 종류
CREATE TABLE IF NOT EXISTS `dessert_category` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '데이터 고유 식별자',
    `category_name` VARCHAR(255) NOT NULL COMMENT '카테고리 이름 (예: 빵, 쿠키)',
    `display_order` INT NULL DEFAULT 0 COMMENT '카테고리의 정렬 우선도',
    `is_active` BOOLEAN NULL DEFAULT TRUE COMMENT '카테고리의 활성화/비활성화',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 시각',
    `deleted_at` DATETIME NULL DEFAULT NULL COMMENT '삭제 시각(논리 삭제)',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='디저트/메뉴 카테고리 종류';

-- 카페 메뉴
CREATE TABLE IF NOT EXISTS `cafe_menu` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '데이터 고유 식별자',
    `cafe_id` INT NOT NULL COMMENT '해당 메뉴가 등록된 카페의 id',
    `menu_name` VARCHAR(255) NOT NULL COMMENT '메뉴 이름',
    `menu_description` VARCHAR(255) NULL COMMENT '메뉴 설명',
    `menu_price` DECIMAL(10,2) NULL COMMENT '메뉴의 가격',
    `menu_stock` INT NULL COMMENT '메뉴 재고',
    `category_id` INT NULL COMMENT '메뉴 카테고리',
    `menu_img_url` VARCHAR(2048) NULL COMMENT '메뉴 대표 이미지',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 시각',
    `deleted_at` DATETIME NULL DEFAULT NULL COMMENT '삭제 시각(논리 삭제)',
    PRIMARY KEY (`id`),
    CONSTRAINT fk_cafe_menu_cafe FOREIGN KEY (cafe_id) REFERENCES `cafe` (id),
    CONSTRAINT fk_cafe_menu_category FOREIGN KEY (category_id) REFERENCES `dessert_category` (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='카페 메뉴';

-- 유저가 수집한 스티커 (테이스팅 노트는 dessert 테이블에 붙인다)
CREATE TABLE IF NOT EXISTS `sticker` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '데이터 고유 식별자',
    `user_id` INT NOT NULL COMMENT '해당 스티커를 수집한 유저 id',
    `sticker_name` VARCHAR(255) NOT NULL COMMENT '스티커 이름',
    `sticker_img_url` VARCHAR(2048) NOT NULL COMMENT '스티커 누끼 이미지',
    `sticker_original_img_url` VARCHAR(2048) NULL COMMENT '스티커 원본 이미지',
    `is_present` BOOLEAN NULL COMMENT '선물받은 스티커인지 구분',
    `source_sticker_id` INT NULL COMMENT '선물받았다면 원본 스티커 id',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 시각',
    `deleted_at` DATETIME NULL DEFAULT NULL COMMENT '삭제 시각(논리 삭제)',
    PRIMARY KEY (`id`),
    CONSTRAINT fk_sticker_user FOREIGN KEY (user_id) REFERENCES `user` (id),
    CONSTRAINT fk_sticker_source FOREIGN KEY (source_sticker_id) REFERENCES `sticker` (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='유저가 수집한 스티커 (테이스팅 노트는 dessert 테이블에 붙인다)';

-- 유저 개인 디저트 기록 = 테이스팅 노트
CREATE TABLE IF NOT EXISTS `dessert` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '데이터 고유 식별자',
    `user_id` INT NOT NULL COMMENT '해당 디저트를 등록한 유저 id',
    `cafe_id` INT NULL COMMENT '유저가 해당 카페의 메뉴라고 등록했다면 삽입',
    `sticker_id` INT NOT NULL COMMENT '디저트 스티커 id (sticker.user_id 와 dessert.user_id 는 같아야 함)',
    `dessert_name` VARCHAR(255) NOT NULL COMMENT '디저트 이름',
    `dessert_description` VARCHAR(2048) NULL COMMENT '디저트 설명 = 테이스팅 노트',
    `dessert_category_id` INT NULL COMMENT '디저트 카테고리',
    `visit_date` DATE NULL COMMENT '방문일자',
    `rating` DECIMAL(2,1) NULL COMMENT '별점',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 시각',
    `deleted_at` DATETIME NULL DEFAULT NULL COMMENT '삭제 시각(논리 삭제)',
    PRIMARY KEY (`id`),
    UNIQUE KEY uq_dessert_sticker (sticker_id),
    CONSTRAINT fk_dessert_user FOREIGN KEY (user_id) REFERENCES `user` (id),
    CONSTRAINT fk_dessert_cafe FOREIGN KEY (cafe_id) REFERENCES `cafe` (id),
    CONSTRAINT fk_dessert_sticker FOREIGN KEY (sticker_id) REFERENCES `sticker` (id),
    CONSTRAINT fk_dessert_category FOREIGN KEY (dessert_category_id) REFERENCES `dessert_category` (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='유저 개인 디저트 기록 = 테이스팅 노트';

-- 테이스팅 노트 이미지 (display_order 로 순서 관리)
CREATE TABLE IF NOT EXISTS `dessert_image` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '데이터 고유 식별자',
    `dessert_id` INT NOT NULL COMMENT '어떤 테이스팅 노트의 이미지인지',
    `image_url` VARCHAR(2048) NOT NULL COMMENT '이미지 url',
    `display_order` INT NOT NULL COMMENT '이미지 순서',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 시각',
    `deleted_at` DATETIME NULL DEFAULT NULL COMMENT '삭제 시각(논리 삭제)',
    PRIMARY KEY (`id`),
    CONSTRAINT fk_dessert_image_dessert FOREIGN KEY (dessert_id) REFERENCES `dessert` (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='테이스팅 노트 이미지 (display_order 로 순서 관리)';

-- 유저가 공유할 수 있는 플레이트
CREATE TABLE IF NOT EXISTS `dessert_plate` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '데이터 고유 식별자',
    `user_id` INT NOT NULL COMMENT '플레이트 소유자',
    `plate_order` INT NOT NULL COMMENT '플레이트 표시 순서',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 시각',
    `deleted_at` DATETIME NULL DEFAULT NULL COMMENT '삭제 시각(논리 삭제)',
    `active_flag` TINYINT GENERATED ALWAYS AS (IF(`deleted_at` IS NULL, 1, NULL)) VIRTUAL COMMENT '삭제 안 된 행이면 1, 삭제된 행이면 NULL (UNIQUE 키에 함께 넣어 삭제된 값은 다시 쓸 수 있게 함. 직접 입력하지 않는다)',
    PRIMARY KEY (`id`),
    UNIQUE KEY uq_plate_user_order (user_id, plate_order, active_flag),
    CONSTRAINT fk_plate_user FOREIGN KEY (user_id) REFERENCES `user` (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='유저가 공유할 수 있는 플레이트';

-- 플레이트에 올린 스티커와 배치 (삭제 시 실제로 행을 지운다. deleted_at 미사용)
CREATE TABLE IF NOT EXISTS `plate_sticker` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '데이터 고유 식별자',
    `plate_id` INT NOT NULL COMMENT '배치될 플레이트 id',
    `sticker_id` INT NOT NULL COMMENT '배치될 스티커 id',
    `position_x` DECIMAL(8,2) NOT NULL COMMENT 'x좌표',
    `position_y` DECIMAL(8,2) NOT NULL COMMENT 'y좌표',
    `rotation` DECIMAL(6,2) NOT NULL COMMENT '회전 각도',
    `display_order` INT NOT NULL COMMENT '표시될 순서',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 시각',
    `deleted_at` DATETIME NULL DEFAULT NULL COMMENT '삭제 시각(논리 삭제)',
    PRIMARY KEY (`id`),
    UNIQUE KEY uq_plate_sticker (plate_id, sticker_id),
    CONSTRAINT fk_plate_sticker_plate FOREIGN KEY (plate_id) REFERENCES `dessert_plate` (id),
    CONSTRAINT fk_plate_sticker_sticker FOREIGN KEY (sticker_id) REFERENCES `sticker` (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='플레이트에 올린 스티커와 배치 (삭제 시 실제로 행을 지운다. deleted_at 미사용)';

-- 스티커 공유 정보
CREATE TABLE IF NOT EXISTS `sticker_share` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '데이터 고유 식별자',
    `sender_id` INT NOT NULL COMMENT '스티커를 보내는 사용자',
    `receiver_id` INT NOT NULL COMMENT '스티커를 받는 사용자',
    `sticker_id` INT NOT NULL COMMENT '보내는 스티커 id',
    `sticker_send_message` VARCHAR(2048) NULL COMMENT '스티커 공유 요청 시의 메시지',
    `share_status` VARCHAR(255) NOT NULL COMMENT 'PENDING / ACCEPTED / REJECTED',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 시각',
    `deleted_at` DATETIME NULL DEFAULT NULL COMMENT '삭제 시각(논리 삭제)',
    PRIMARY KEY (`id`),
    CONSTRAINT fk_share_sender FOREIGN KEY (sender_id) REFERENCES `user` (id),
    CONSTRAINT fk_share_receiver FOREIGN KEY (receiver_id) REFERENCES `user` (id),
    CONSTRAINT fk_share_sticker FOREIGN KEY (sticker_id) REFERENCES `sticker` (id),
    CONSTRAINT ck_share_status CHECK (share_status IN ('PENDING','ACCEPTED','REJECTED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='스티커 공유 정보';

-- 스티커 공유 요청
CREATE TABLE IF NOT EXISTS `sticker_request` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '데이터 고유 식별자',
    `requester_id` INT NOT NULL COMMENT '공유를 요청한 사용자',
    `request_receiver_id` INT NOT NULL COMMENT '공유 요청을 받은 사용자',
    `sticker_id` INT NOT NULL COMMENT '공유 요청 스티커 id',
    `sticker_request_message` VARCHAR(2048) NULL COMMENT '스티커 공유 요청 시의 메시지',
    `request_status` VARCHAR(255) NOT NULL COMMENT 'PENDING / ACCEPTED / REJECTED',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 시각',
    `deleted_at` DATETIME NULL DEFAULT NULL COMMENT '삭제 시각(논리 삭제)',
    PRIMARY KEY (`id`),
    CONSTRAINT fk_sreq_requester FOREIGN KEY (requester_id) REFERENCES `user` (id),
    CONSTRAINT fk_sreq_receiver FOREIGN KEY (request_receiver_id) REFERENCES `user` (id),
    CONSTRAINT fk_sreq_sticker FOREIGN KEY (sticker_id) REFERENCES `sticker` (id),
    CONSTRAINT ck_sreq_status CHECK (request_status IN ('PENDING','ACCEPTED','REJECTED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='스티커 공유 요청';

-- 태그 종류
CREATE TABLE IF NOT EXISTS `tag` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '데이터 고유 식별자',
    `tag_name` VARCHAR(255) NOT NULL COMMENT '태그 이름',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 시각',
    `deleted_at` DATETIME NULL DEFAULT NULL COMMENT '삭제 시각(논리 삭제)',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='태그 종류';

-- 디저트의 태그
CREATE TABLE IF NOT EXISTS `dessert_tag` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '데이터 고유 식별자',
    `dessert_id` INT NOT NULL COMMENT '태그를 달 대상',
    `tag_id` INT NOT NULL COMMENT '태그 id',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 시각',
    `deleted_at` DATETIME NULL DEFAULT NULL COMMENT '삭제 시각(논리 삭제)',
    PRIMARY KEY (`id`),
    CONSTRAINT fk_dessert_tag_dessert FOREIGN KEY (dessert_id) REFERENCES `dessert` (id),
    CONSTRAINT fk_dessert_tag_tag FOREIGN KEY (tag_id) REFERENCES `tag` (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='디저트의 태그';

-- 스티커의 태그
CREATE TABLE IF NOT EXISTS `sticker_tag` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '데이터 고유 식별자',
    `sticker_id` INT NOT NULL COMMENT '태그를 달 대상',
    `tag_id` INT NOT NULL COMMENT '태그 id',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 시각',
    `deleted_at` DATETIME NULL DEFAULT NULL COMMENT '삭제 시각(논리 삭제)',
    PRIMARY KEY (`id`),
    CONSTRAINT fk_sticker_tag_sticker FOREIGN KEY (sticker_id) REFERENCES `sticker` (id),
    CONSTRAINT fk_sticker_tag_tag FOREIGN KEY (tag_id) REFERENCES `tag` (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='스티커의 태그';

-- 메뉴의 태그
CREATE TABLE IF NOT EXISTS `menu_tag` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '데이터 고유 식별자',
    `cafe_menu_id` INT NOT NULL COMMENT '태그를 달 대상',
    `tag_id` INT NOT NULL COMMENT '태그 id',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 시각',
    `deleted_at` DATETIME NULL DEFAULT NULL COMMENT '삭제 시각(논리 삭제)',
    PRIMARY KEY (`id`),
    CONSTRAINT fk_menu_tag_menu FOREIGN KEY (cafe_menu_id) REFERENCES `cafe_menu` (id),
    CONSTRAINT fk_menu_tag_tag FOREIGN KEY (tag_id) REFERENCES `tag` (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='메뉴의 태그';

-- 게시글 카테고리
CREATE TABLE IF NOT EXISTS `post_category` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '데이터 고유 식별자',
    `category_name` VARCHAR(255) NOT NULL COMMENT '카테고리 이름 (예: 게시글, 리뷰, 테이스팅 노트)',
    `category_description` VARCHAR(255) NULL COMMENT '카테고리 설명',
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE COMMENT '카테고리 활성화 여부',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 시각',
    `deleted_at` DATETIME NULL DEFAULT NULL COMMENT '삭제 시각(논리 삭제)',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='게시글 카테고리';

-- 유저가 작성한 게시글 (이미지는 post_image)
CREATE TABLE IF NOT EXISTS `post` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '데이터 고유 식별자',
    `user_id` INT NOT NULL COMMENT '글 등록한 유저 id',
    `post_category_id` INT NOT NULL COMMENT '게시글 종류',
    `cafe_id` INT NULL COMMENT '방문한 카페 id',
    `dessert_id` INT NULL COMMENT '등록한 디저트 id',
    `cafe_menu_id` INT NULL COMMENT '카페 메뉴 id',
    `visit_date` DATE NULL COMMENT '방문일자',
    `rating` DECIMAL(2,1) NULL COMMENT '별점',
    `post_title` VARCHAR(2048) NOT NULL COMMENT '게시글 제목',
    `post_text` TEXT NULL COMMENT '본문 내용',
    `sticker_id` INT NULL COMMENT '가져올 스티커 id',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 시각',
    `deleted_at` DATETIME NULL DEFAULT NULL COMMENT '삭제 시각(논리 삭제)',
    PRIMARY KEY (`id`),
    CONSTRAINT fk_post_user FOREIGN KEY (user_id) REFERENCES `user` (id),
    CONSTRAINT fk_post_category FOREIGN KEY (post_category_id) REFERENCES `post_category` (id),
    CONSTRAINT fk_post_cafe FOREIGN KEY (cafe_id) REFERENCES `cafe` (id),
    CONSTRAINT fk_post_dessert FOREIGN KEY (dessert_id) REFERENCES `dessert` (id),
    CONSTRAINT fk_post_menu FOREIGN KEY (cafe_menu_id) REFERENCES `cafe_menu` (id),
    CONSTRAINT fk_post_sticker FOREIGN KEY (sticker_id) REFERENCES `sticker` (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='유저가 작성한 게시글 (이미지는 post_image)';

-- 게시글 이미지
CREATE TABLE IF NOT EXISTS `post_image` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '데이터 고유 식별자',
    `post_id` INT NOT NULL COMMENT '게시글 id',
    `image_url` VARCHAR(2048) NOT NULL COMMENT '이미지 url',
    `display_order` INT NOT NULL COMMENT '이미지 순서',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 시각',
    `deleted_at` DATETIME NULL DEFAULT NULL COMMENT '삭제 시각(논리 삭제)',
    PRIMARY KEY (`id`),
    CONSTRAINT fk_post_image_post FOREIGN KEY (post_id) REFERENCES `post` (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='게시글 이미지';

-- 게시글 댓글
CREATE TABLE IF NOT EXISTS `comment` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '데이터 고유 식별자',
    `user_id` INT NOT NULL COMMENT '댓글 작성 유저',
    `post_id` INT NOT NULL COMMENT '댓글이 달린 글',
    `parent_comment_id` INT NULL COMMENT '답글이면 원 댓글 id',
    `comment_text` TEXT NOT NULL COMMENT '댓글 내용',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 시각',
    `deleted_at` DATETIME NULL DEFAULT NULL COMMENT '삭제 시각(논리 삭제)',
    PRIMARY KEY (`id`),
    CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES `user` (id),
    CONSTRAINT fk_comment_post FOREIGN KEY (post_id) REFERENCES `post` (id),
    CONSTRAINT fk_comment_parent FOREIGN KEY (parent_comment_id) REFERENCES `comment` (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='게시글 댓글';

-- 게시글 좋아요 (삭제 시 실제로 행을 지운다. deleted_at 미사용)
CREATE TABLE IF NOT EXISTS `post_like` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '데이터 고유 식별자',
    `user_id` INT NOT NULL COMMENT '좋아요를 남긴 유저',
    `post_id` INT NOT NULL COMMENT '좋아요가 달린 글',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 시각',
    `deleted_at` DATETIME NULL DEFAULT NULL COMMENT '삭제 시각(논리 삭제)',
    PRIMARY KEY (`id`),
    UNIQUE KEY uq_post_like (user_id, post_id),
    CONSTRAINT fk_post_like_user FOREIGN KEY (user_id) REFERENCES `user` (id),
    CONSTRAINT fk_post_like_post FOREIGN KEY (post_id) REFERENCES `post` (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='게시글 좋아요 (삭제 시 실제로 행을 지운다. deleted_at 미사용)';

-- 댓글 좋아요 (삭제 시 실제로 행을 지운다. deleted_at 미사용)
CREATE TABLE IF NOT EXISTS `comment_like` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '데이터 고유 식별자',
    `user_id` INT NOT NULL COMMENT '좋아요를 남긴 유저',
    `comment_id` INT NOT NULL COMMENT '좋아요가 달린 댓글',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 시각',
    `deleted_at` DATETIME NULL DEFAULT NULL COMMENT '삭제 시각(논리 삭제)',
    PRIMARY KEY (`id`),
    UNIQUE KEY uq_comment_like (user_id, comment_id),
    CONSTRAINT fk_comment_like_user FOREIGN KEY (user_id) REFERENCES `user` (id),
    CONSTRAINT fk_comment_like_comment FOREIGN KEY (comment_id) REFERENCES `comment` (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='댓글 좋아요 (삭제 시 실제로 행을 지운다. deleted_at 미사용)';

-- 게시글 북마크 (삭제 시 실제로 행을 지운다. deleted_at 미사용)
CREATE TABLE IF NOT EXISTS `post_mark` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '데이터 고유 식별자',
    `user_id` INT NOT NULL COMMENT '북마크한 유저',
    `post_id` INT NOT NULL COMMENT '북마크 대상 글',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 시각',
    `deleted_at` DATETIME NULL DEFAULT NULL COMMENT '삭제 시각(논리 삭제)',
    PRIMARY KEY (`id`),
    UNIQUE KEY uq_post_mark (user_id, post_id),
    CONSTRAINT fk_post_mark_user FOREIGN KEY (user_id) REFERENCES `user` (id),
    CONSTRAINT fk_post_mark_post FOREIGN KEY (post_id) REFERENCES `post` (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='게시글 북마크 (삭제 시 실제로 행을 지운다. deleted_at 미사용)';

-- 카페 북마크 (삭제 시 실제로 행을 지운다. deleted_at 미사용)
CREATE TABLE IF NOT EXISTS `cafe_mark` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '데이터 고유 식별자',
    `user_id` INT NOT NULL COMMENT '북마크한 유저',
    `cafe_id` INT NOT NULL COMMENT '북마크한 카페',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 시각',
    `deleted_at` DATETIME NULL DEFAULT NULL COMMENT '삭제 시각(논리 삭제)',
    PRIMARY KEY (`id`),
    UNIQUE KEY uq_cafe_mark (user_id, cafe_id),
    CONSTRAINT fk_cafe_mark_user FOREIGN KEY (user_id) REFERENCES `user` (id),
    CONSTRAINT fk_cafe_mark_cafe FOREIGN KEY (cafe_id) REFERENCES `cafe` (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='카페 북마크 (삭제 시 실제로 행을 지운다. deleted_at 미사용)';

-- 친구추가 요청 (친구 코드로 user.id 를 찾아 사용)
CREATE TABLE IF NOT EXISTS `friend_request` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '데이터 고유 식별자',
    `sender_id` INT NOT NULL COMMENT '요청한 유저',
    `receiver_id` INT NOT NULL COMMENT '요청 받은 유저',
    `request_status` VARCHAR(255) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING / ACCEPTED / REJECTED / CANCELED',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 시각',
    `deleted_at` DATETIME NULL DEFAULT NULL COMMENT '삭제 시각(논리 삭제)',
    PRIMARY KEY (`id`),
    CONSTRAINT fk_freq_sender FOREIGN KEY (sender_id) REFERENCES `user` (id),
    CONSTRAINT fk_freq_receiver FOREIGN KEY (receiver_id) REFERENCES `user` (id),
    CONSTRAINT ck_freq_status CHECK (request_status IN ('PENDING','ACCEPTED','REJECTED','CANCELED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='친구추가 요청 (친구 코드로 user.id 를 찾아 사용)';

-- 친구 관계 (A와 B가 친구가 되면 (A, B) 한 줄로 저장)
CREATE TABLE IF NOT EXISTS `friend` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '데이터 고유 식별자',
    `user_id` INT NOT NULL COMMENT '친구 신청 유저',
    `friend_user_id` INT NOT NULL COMMENT '친구 신청을 받은 유저',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 시각',
    `deleted_at` DATETIME NULL DEFAULT NULL COMMENT '삭제 시각(논리 삭제)',
    PRIMARY KEY (`id`),
    CONSTRAINT fk_friend_user FOREIGN KEY (user_id) REFERENCES `user` (id),
    CONSTRAINT fk_friend_friend FOREIGN KEY (friend_user_id) REFERENCES `user` (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='친구 관계 (A와 B가 친구가 되면 (A, B) 한 줄로 저장)';

-- 알림
CREATE TABLE IF NOT EXISTS `notification` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '데이터 고유 식별자',
    `user_id` INT NOT NULL COMMENT '알림을 받는 유저',
    `sender_id` INT NULL COMMENT '알림을 보낸 사용자 (시스템이면 NULL)',
    `notification_type` VARCHAR(255) NULL COMMENT '알림 유형 (FRIEND_REQUEST, STICKER_REQUEST, STICKER_SHARE, SYSTEM, LIKE 등). FRIEND_REQUEST 는 실제 삭제, 그 외는 논리 삭제',
    `notification_text` VARCHAR(2048) NOT NULL COMMENT '알림 본문',
    `is_read` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '유저가 확인했는지 여부',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 시각',
    `deleted_at` DATETIME NULL DEFAULT NULL COMMENT '삭제 시각(논리 삭제)',
    PRIMARY KEY (`id`),
    CONSTRAINT fk_noti_user FOREIGN KEY (user_id) REFERENCES `user` (id),
    CONSTRAINT fk_noti_sender FOREIGN KEY (sender_id) REFERENCES `user` (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='알림';
