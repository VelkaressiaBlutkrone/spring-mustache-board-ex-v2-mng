package com.example.v2.exception.exs;

/**
 * 게시글을 찾을 수 없을 때 발생하는 예외.
 *
 * BoardService.board(id) 에서 존재하지 않는 게시글 ID 로 조회 시 발생한다.
 * 기본 메시지: "게시글을 찾을 수 없습니다. (id: {id})"
 */
public class BoardNotFoundException extends RuntimeException {

    public BoardNotFoundException(Long id) {
        super("게시글을 찾을 수 없습니다. (id: " + id + ")");
    }

    public BoardNotFoundException(String message) {
        super(message);
    }
}
