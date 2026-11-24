-- 이미 만들어 둔 DB 에 post_title 컬럼만 추가할 때 실행합니다. (새로 만드는 DB 는 dessertrip_schema.sql 에 이미 포함)
-- 기존 게시글에는 임시 제목을 채운 뒤 기본값을 없애 NOT NULL 만 남깁니다.
USE dessertrip;
ALTER TABLE `post`
    ADD COLUMN `post_title` VARCHAR(2048) NOT NULL DEFAULT '' COMMENT '게시글 제목' AFTER `rating`;
UPDATE `post` SET `post_title` = '테이스팅 노트를 작성했어요!' WHERE `post_title` = '';
ALTER TABLE `post` ALTER COLUMN `post_title` DROP DEFAULT;
