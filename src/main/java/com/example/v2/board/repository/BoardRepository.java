package com.example.v2.board.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.v2.board.entity.Board;

/**
 * 게시판 JPA 리포지토리.
 *
 * JpaRepository<Board, Long> 을 상속하면 기본 CRUD 메서드가 자동 제공된다.
 * - save(), findById(), findAll(), delete() 등
 *
 * 아래 메서드들은 Spring Data JPA 의 '쿼리 메서드' 기능을 활용하여
 * 메서드명만으로 자동으로 JPQL 쿼리를 생성한다.
 */
public interface BoardRepository extends JpaRepository<Board, Long> {

    /** 전체 게시글을 작성일 내림차순으로 페이징 조회한다. */
    Page<Board> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /** 제목에 키워드가 포함된 게시글을 페이징 조회한다. */
    Page<Board> findByTitleContaining(String title, Pageable pageable);

    /** 작성자에 키워드가 포함된 게시글을 페이징 조회한다. */
    Page<Board> findByWriterContaining(String writer, Pageable pageable);

    /** 제목 또는 내용에 키워드가 포함된 게시글을 페이징 조회한다. */
    Page<Board> findByTitleContainingOrContentContaining(String title, String content, Pageable pageable);
}
