package com.example.v2.board.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.v2.board.entity.Board;

/**
 * 게시판 커스텀 리포지토리 인터페이스.
 *
 * Spring Data JPA 의 쿼리 메서드만으로 구현하기 어려운
 * 복잡한 동적 쿼리를 정의하기 위한 인터페이스이다.
 *
 * 향후 QueryDSL 또는 Criteria API 등으로 구현체를 작성하여
 * 복합 검색(제목+내용+작성자 동시 검색) 등을 처리할 수 있다.
 */
public interface BoardRepositoryCustom {

    /** 전체 게시글을 작성일 내림차순으로 페이징 조회한다. */
    Page<Board> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /** 제목, 내용, 작성자 조건으로 게시글을 검색한다. */
    Page<Board> findAllByOrderByCreatedAtDesc(String title, String content, String writer, Pageable pageable);
}
