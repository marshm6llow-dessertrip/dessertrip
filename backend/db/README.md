# DB 스크립트

1. (이전 버전을 실행한 적이 있다면) `drop_old_tables.sql` — 예전 자동 생성 테이블 정리
2. `dessertrip_schema.sql` — 테이블 명세서 기준 27개 테이블 생성 (DB 이름: dessertrip)
   - 이전에 만든 DB 를 그대로 쓴다면 대신 `alter_post_title.sql`, `alter_sticker_messages.sql` 을 한 번씩 실행 (새 DB 는 불필요)
3. `application-local.properties` 에서 `spring.jpa.hibernate.ddl-auto=none` 으로 바꾸기 (테이블은 SQL 로만 만든다)
4. 서버 실행 → local 프로필에서 기본 데이터(칭호, 카테고리, 태그, 게시글 종류, 샘플 카페, 개발용 사용자)가 자동으로 채워진다

DB 계정/비밀번호는 이 폴더에 적지 않는다 (application-local.properties 또는 환경변수).
