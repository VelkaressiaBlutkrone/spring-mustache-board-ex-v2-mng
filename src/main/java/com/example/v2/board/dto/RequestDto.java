package com.example.v2.board.dto;

import com.example.v2.board.entity.Board;

import lombok.Data;

/**
 * 게시글 작성 요청 DTO (폼 전송용).
 *
 * HTML 폼에서 POST /board/write 로 전송되는 데이터를 바인딩한다.
 * 
 * @ModelAttribute 로 컨트롤러에서 자동 바인딩된다.
 *
 * @Data 는 @Getter, @Setter, @ToString, @EqualsAndHashCode 를 포함하는 Lombok
 *       어노테이션이다.
 */
@Data
public class RequestDto {
    /** 게시글 제목 */
    String title;
    /** 게시글 내용 */
    String content;
    /** 작성자 이름 */
    String writer;
    /** 서버에 저장된 파일명 (서비스 레이어에서 세팅) */
    String fileName;
    /** 원본 파일명 (서비스 레이어에서 세팅) */
    String originalFileName;

    /** DTO를 Board 엔티티로 변환한다. */
    public Board toEntity() {
        return Board.builder()
                .title(this.title)
                .content(this.content)
                .writer(this.writer)
                .fileName(this.fileName)
                .originalFileName(this.originalFileName)
                .build();
    }
}
