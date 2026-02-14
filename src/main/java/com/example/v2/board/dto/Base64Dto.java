package com.example.v2.board.dto;

import com.example.v2.board.entity.Board;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Base64 이미지 업로드 요청 DTO (REST API JSON 전송용).
 *
 * 클라이언트에서 Fetch API 를 통해 이미지를 Base64 문자열로 인코딩하여
 * JSON 으로 전송할 때 사용되는 DTO 이다.
 * POST /board/api/write 에서 @RequestBody 로 바인딩된다.
 */
@Data
@AllArgsConstructor
public class Base64Dto {
    /** 게시글 제목 */
    String title;
    /** 게시글 내용 */
    String content;
    /** 작성자 이름 */
    String writer;
    /** Base64 인코딩된 이미지 데이터 문자열 */
    String imageBase64;
    /** 원본 이미지 파일명 (확장자 추출에 사용) */
    String imageFileName;

    /** DTO를 Board 엔티티로 변환한다. */
    public Board toEntity() {
        return Board.builder()
                .title(this.title)
                .content(this.content)
                .writer(this.writer)
                .profileImage(imageBase64)
                .build();
    }
}
