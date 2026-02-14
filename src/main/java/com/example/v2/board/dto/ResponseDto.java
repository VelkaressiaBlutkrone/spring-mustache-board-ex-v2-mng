package com.example.v2.board.dto;

import java.time.format.DateTimeFormatter;

import com.example.v2.board.entity.Board;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 게시글 응답 DTO.
 *
 * Board 엔티티의 데이터를 뷰(Mustache 템플릿)에 전달하기 위한 DTO이다.
 * 엔티티를 직접 뷰에 노출하지 않고, 필요한 필드만 선별하여 전달한다.
 * createdAt 을 "yyyy-MM-dd HH:mm" 포맷 문자열로 변환하여 뷰에서 바로 사용할 수 있도록 한다.
 */
@Getter
@AllArgsConstructor
public class ResponseDto {
    Long id;
    String title;
    String content;
    String writer;
    String fileName;
    String originalFileName;
    String profileImage;
    Long viewCount;
    /** 포맷팅된 작성일시 문자열 (예: "2025-01-15 14:30") */
    String createdAt;

    /** 날짜 포맷터: "yyyy-MM-dd HH:mm" 형식으로 출력 */
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /** Board 엔티티를 ResponseDto로 변환한다. */
    public ResponseDto(Board board) {
        this.id = board.getId();
        this.title = board.getTitle();
        this.content = board.getContent();
        this.writer = board.getWriter();
        this.fileName = board.getFileName();
        this.profileImage = board.getProfileImage();
        this.originalFileName = board.getOriginalFileName();
        this.viewCount = board.getViewCount();
        this.createdAt = board.getCreatedAt().format(formatter);
    }
}
