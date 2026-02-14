package com.example.board.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.v2.board.entity.Board;
import com.example.v2.board.repository.BoardRepository;
import com.example.v2.board.service.BoardService;

@SpringBootTest
@Transactional
class BoardServiceTest {

    @Autowired
    BoardService boardService;

    @Autowired
    BoardRepository boardRepository;

    @Test
    void detail_호출시_viewCount_증가() {
        Board saved = boardRepository.save(Board.builder()
                .title("테스트 제목")
                .content("테스트 내용")
                .writer("테스터")
                .viewCount(0L)
                .build());

        Long id = saved.getId();
        Long beforeCount = saved.getViewCount();

        Board result = boardService.board(id);

        assertThat(result.getViewCount()).isEqualTo(beforeCount + 1);

        Board after = boardRepository.findById(id).orElseThrow();
        assertThat(after.getViewCount()).isEqualTo(beforeCount + 1);
    }
}
