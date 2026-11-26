-- 이미 만들어 둔 DB 에 스티커 공유/요청 메시지 컬럼만 추가할 때 실행합니다. (새로 만드는 DB 는 dessertrip_schema.sql 에 이미 포함)
-- 메시지는 선택 입력이라 NULL 을 허용하며, 기존 행은 NULL 로 남습니다.
-- 이미 컬럼이 있는 DB 에서 다시 실행하면 "Duplicate column name" 오류가 나므로 한 번만 실행하세요.
USE dessertrip;
ALTER TABLE `sticker_share`
    ADD COLUMN `sticker_send_message` VARCHAR(2048) NULL COMMENT '스티커 공유 요청 시의 메시지' AFTER `sticker_id`;
ALTER TABLE `sticker_request`
    ADD COLUMN `sticker_request_message` VARCHAR(2048) NULL COMMENT '스티커 공유 요청 시의 메시지' AFTER `sticker_id`;
