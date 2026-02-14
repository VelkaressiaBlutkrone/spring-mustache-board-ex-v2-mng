package com.example.v2.exception.exs;

/**
 * 중복된 제목으로 게시글을 저장하려 할 때 발생하는 예외.
 *
 * Board 엔티티의 title 컬럼에 UNIQUE 제약 조건이 설정되어 있으므로,
 * 동일한 제목이 이미 존재하면 DataIntegrityViolationException 대신 이 예외를 던진다.
 */
public class DuplicateTitleException extends RuntimeException {

    public DuplicateTitleException(String title) {
        super("이미 존재하는 제목입니다. (title: " + title + ")");
    }

    public DuplicateTitleException(String message, Throwable cause) {
        super(message, cause);
    }
}
