-- 이전 버전 백엔드(JPA ddl-auto=update 로 자동 생성되던 테이블)를 정리하는 스크립트.
-- 명세서 기준 테이블(dessertrip_schema.sql)로 옮겨 가기 전에, 개발 DB 에서 한 번만 실행한다.
-- 주의: 아래 테이블의 데이터(예전에 만든 스티커/노트)는 사라진다. 운영/공유 DB 에서는 실행하지 말 것.
USE dessertrip;
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS plate_stickers;
DROP TABLE IF EXISTS dessert_photos;
DROP TABLE IF EXISTS dessert_tags;
DROP TABLE IF EXISTS desserts;
DROP TABLE IF EXISTS user_profiles;
DROP TABLE IF EXISTS places;
DROP TABLE IF EXISTS tags;
DROP TABLE IF EXISTS categories;
SET FOREIGN_KEY_CHECKS = 1;
