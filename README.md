# Spring Boot Mustache 게시판 예제

Spring Boot 4.0 + Mustache 템플릿 엔진을 활용한 CRUD 게시판 애플리케이션입니다.

## 기술 스택

- **Java 21**
- **Spring Boot 4.0.2**
- **Spring Data JPA** - ORM 및 데이터 액세스
- **Mustache** - 서버 사이드 템플릿 엔진
- **H2 Database** - 인메모리 데이터베이스
- **Lombok** - 보일러플레이트 코드 제거
- **Gradle 9.3.0** - 빌드 도구

## 주요 기능

- 게시글 목록 조회 (페이징 지원, 10개 단위 페이지 블록)
- 게시글 검색 (제목 / 작성자 / 제목+내용)
- 게시글 상세 조회 (조회수 자동 증가)
- 게시글 작성 (폼 전송 / Fetch API + Base64 전송)
- 파일 첨부 (MultipartFile 업로드 / Base64 이미지 업로드)

## 프로젝트 구조

```
src/main/java/com/example/board/
├── SpringMustacheBoardExampleApplication.java   # 애플리케이션 진입점
├── config/
│   └── WebConfig.java                           # 정적 리소스 매핑 (/images/**)
├── controller/
│   ├── BoardController.java                     # 페이지 컨트롤러 (SSR)
│   └── BoardRestController.java                 # REST API 컨트롤러 (Base64 업로드)
├── dto/
│   ├── RequestDto.java                          # 게시글 작성 요청 DTO (폼 전송용)
│   ├── ResponseDto.java                         # 게시글 응답 DTO (날짜 포맷팅 포함)
│   ├── Base64Dto.java                           # Base64 이미지 업로드 DTO (REST API용)
│   └── PagingDto.java                           # 페이징 네비게이션 DTO (10개 단위 블록)
├── entity/
│   └── Board.java                               # 게시판 JPA 엔티티
├── repository/
│   ├── BoardRepository.java                     # Spring Data JPA 리포지토리
│   └── BoardRepositoryCustom.java               # 커스텀 리포지토리 인터페이스
└── service/
    └── BoardService.java                        # 비즈니스 로직 (파일 업로드 포함)

src/main/resources/
├── application.properties                       # 애플리케이션 설정
└── templates/board/
    ├── list.mustache                            # 게시글 목록 페이지
    ├── detail.mustache                          # 게시글 상세 페이지
    └── write.mustache                           # 게시글 작성 페이지

src/test/java/com/example/board/
├── SpringMustacheBoardExampleApplicationTests.java  # 컨텍스트 로드 테스트
└── service/
    └── BoardServiceTest.java                        # 조회수 증가 테스트
```

## API 엔드포인트

### 페이지 (BoardController)

| Method | URL                  | 설명                              |
| ------ | -------------------- | --------------------------------- |
| GET    | `/`        | 게시글 목록 (페이징, 검색)        |
| GET    | `/board/detail/{id}` | 게시글 상세                       |
| GET    | `/board/write`       | 글쓰기 페이지                     |
| POST   | `/board/write`       | 게시글 저장 (multipart/form-data) |

### REST API (BoardRestController)

| Method | URL                | 설명                                           |
| ------ | ------------------ | ---------------------------------------------- |
| POST   | `/board/api/write` | 게시글 저장 (JSON + Base64 이미지, 303 리다이렉트) |

## 실행 방법

```bash
# 프로젝트 빌드
./gradlew build

# 애플리케이션 실행
./gradlew bootRun
```

실행 후 브라우저에서 `http://localhost:8080/`로 접속합니다.

## 설정

`application.properties`에서 주요 설정을 변경할 수 있습니다.

### 데이터소스 / H2

| 항목                            | 기본값                | 설명                  |
| ------------------------------- | --------------------- | --------------------- |
| `spring.datasource.url`        | `jdbc:h2:mem:boarddb` | H2 인메모리 DB URL    |
| `spring.datasource.username`   | `sa`                  | DB 접속 사용자        |
| `spring.datasource.password`   | *(빈 값)*             | DB 접속 비밀번호      |
| `spring.h2.console.enabled`    | `true`                | H2 웹 콘솔 활성화    |
| `spring.h2.console.path`       | `/h2-console`         | H2 콘솔 경로          |

### JPA / Hibernate

| 항목                                        | 기본값   | 설명                       |
| ------------------------------------------- | -------- | -------------------------- |
| `spring.jpa.hibernate.ddl-auto`             | `create` | DDL 자동 생성 전략         |
| `spring.jpa.show-sql`                       | `true`   | 실행 SQL 콘솔 출력         |
| `spring.jpa.properties.hibernate.format_sql`| `true`   | SQL 포맷팅 출력            |

### 파일 업로드

| 항목                                        | 기본값                 | 설명                |
| ------------------------------------------- | ---------------------- | ------------------- |
| `file.upload-dir`                           | `${user.dir}/uploads/` | 파일 업로드 디렉토리 |
| `spring.servlet.multipart.max-file-size`    | `10MB`                 | 단일 파일 최대 크기  |
| `spring.servlet.multipart.max-request-size` | `10MB`                 | 전체 요청 최대 크기  |

### Mustache 템플릿

| 항목                                                  | 기본값 | 설명                                 |
| ----------------------------------------------------- | ------ | ------------------------------------ |
| `spring.mustache.servlet.expose-session-attributes`   | `true` | HttpSession attribute 템플릿 노출    |
| `spring.mustache.servlet.expose-request-attributes`   | `true` | HttpServletRequest attribute 템플릿 노출 |

## 데이터베이스

H2 인메모리 데이터베이스를 사용하며, 애플리케이션 실행 시 테이블이 자동 생성됩니다.

H2 콘솔: `http://localhost:8080/h2-console`

- JDBC URL: `jdbc:h2:mem:boarddb`
- Username: `sa`
- Password: *(빈 값)*

### Board 테이블 구조

| 컬럼                 | 타입                | 설명                 |
| -------------------- | ------------------- | -------------------- |
| `id`                 | BIGINT (PK, AUTO)   | 게시글 ID            |
| `title`              | VARCHAR(50), UNIQUE | 제목                 |
| `content`            | CLOB                | 내용                 |
| `writer`             | VARCHAR(50)         | 작성자               |
| `file_name`          | VARCHAR(500)        | 저장된 파일명 (UUID) |
| `original_file_name` | VARCHAR(500)        | 원본 파일명          |
| `profile_image`      | CLOB                | Base64 인코딩 이미지 |
| `view_count`         | BIGINT              | 조회수 (기본값 0)    |
| `create_at`          | TIMESTAMP           | 작성일시 (자동 생성) |
