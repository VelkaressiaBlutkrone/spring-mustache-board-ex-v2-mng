package com.example.v2.board.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 게시판 엔티티 클래스.
 *
 * JPA @Entity 로 등록되어 H2 데이터베이스의 "board" 테이블과 매핑된다.
 * Lombok 어노테이션으로 보일러플레이트 코드를 줄인다.
 *
 * - @AllArgsConstructor : 모든 필드를 매개변수로 받는 생성자 자동 생성
 * - @NoArgsConstructor : 기본 생성자 자동 생성 (JPA 스펙에서 필수)
 * - @Builder : 빌더 패턴을 사용한 객체 생성 지원
 * - @Getter : 모든 필드의 getter 메서드 자동 생성
 */
@Entity
@Table(name = "board")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Board {

    /** 게시글 고유 ID (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 게시글 제목 */
    @Column(nullable = false, length = 50, unique = true)
    private String title;

    /** 게시글 내용 */
    @Column
    @Lob
    private String content;

    /** 작성자 이름 */
    @Column(nullable = false, length = 50)
    private String writer;

    /** 서버에 저장된 파일명 (UUID + 확장자) */
    @Column(name = "file_name", length = 500)
    private String fileName;

    /** 사용자가 업로드한 원본 파일명 */
    @Column(name = "original_file_name", length = 500)
    private String originalFileName;

    /** 조회수 */
    @Column(name = "view_count")
    private Long viewCount;

    /** Base64 인코딩된 프로필/첨부 이미지 문자열 */
    @Column(name = "profile_image")
    @Lob
    private String profileImage;

    /** 게시글 작성 일시 */
    @Column(name = "create_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    /** 조회수를 1 증가시킨다. */
    public void increaseViewCount() {
        this.viewCount++;
    }

    /** 파일 정보를 설정한다. */
    public void setFileInfo(String fileName, String originalFileName) {
        this.fileName = fileName;
        this.originalFileName = originalFileName;
    }

    /** 엔티티 저장 전 조회수를 초기화한다. */
    @PrePersist
    public void defViewCount() {
        if (viewCount == null) {
            viewCount = 0L;
        }
    }

}
