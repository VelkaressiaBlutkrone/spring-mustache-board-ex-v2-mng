package com.example.v2.board.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.example.v2.board.dto.RequestDto;
import com.example.v2.board.entity.Board;
import com.example.v2.board.repository.BoardRepository;
import com.example.v2.exception.exs.BoardNotFoundException;
import com.example.v2.exception.exs.DuplicateTitleException;

@ExtendWith(MockitoExtension.class)
@DisplayName("BoardService 단위 테스트")
class BoardServiceTest {

    @Mock
    private BoardRepository boardRepository;

    @InjectMocks
    private BoardService boardService;

    @Nested
    @DisplayName("boardList - 게시글 목록 조회")
    class BoardListTest {

        @Test
        @DisplayName("키워드가 없으면 전체 목록을 반환한다")
        void boardList_키워드없으면_전체목록을반환한다() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            List<Board> boards = List.of(
                    createBoard(1L, "제목1", "작성자1"),
                    createBoard(2L, "제목2", "작성자2"));
            Page<Board> boardPage = new PageImpl<>(boards, pageable, boards.size());

            given(boardRepository.findAllByOrderByCreatedAtDesc(pageable))
                    .willReturn(boardPage);

            // when
            Page<Board> result = boardService.boardList("", "", pageable);

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).getTitle()).isEqualTo("제목1");
            then(boardRepository).should().findAllByOrderByCreatedAtDesc(pageable);
        }

        @Test
        @DisplayName("제목으로 검색하면 해당 게시글을 반환한다")
        void boardList_제목으로검색하면_해당게시글을반환한다() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            String keyword = "Spring";
            List<Board> boards = List.of(createBoard(1L, "Spring Boot 가이드", "작성자1"));
            Page<Board> boardPage = new PageImpl<>(boards, pageable, boards.size());

            given(boardRepository.findByTitleContaining(eq(keyword), eq(pageable)))
                    .willReturn(boardPage);

            // when
            Page<Board> result = boardService.boardList("title", keyword, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getTitle()).contains("Spring");
            then(boardRepository).should().findByTitleContaining(keyword, pageable);
        }

        @Test
        @DisplayName("작성자로 검색하면 해당 게시글을 반환한다")
        void boardList_작성자로검색하면_해당게시글을반환한다() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            String keyword = "홍길동";
            List<Board> boards = List.of(createBoard(1L, "테스트 글", "홍길동"));
            Page<Board> boardPage = new PageImpl<>(boards, pageable, boards.size());

            given(boardRepository.findByWriterContaining(eq(keyword), eq(pageable)))
                    .willReturn(boardPage);

            // when
            Page<Board> result = boardService.boardList("writer", keyword, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getWriter()).isEqualTo("홍길동");
            then(boardRepository).should().findByWriterContaining(keyword, pageable);
        }

        @Test
        @DisplayName("제목+내용 검색하면 해당 게시글을 반환한다")
        void boardList_제목내용으로검색하면_해당게시글을반환한다() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            String keyword = "JPA";
            List<Board> boards = List.of(createBoard(1L, "JPA 학습", "작성자1"));
            Page<Board> boardPage = new PageImpl<>(boards, pageable, boards.size());

            given(boardRepository.findByTitleContainingOrContentContaining(
                    eq(keyword), eq(keyword), eq(pageable)))
                    .willReturn(boardPage);

            // when
            Page<Board> result = boardService.boardList("titleContent", keyword, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            then(boardRepository).should()
                    .findByTitleContainingOrContentContaining(keyword, keyword, pageable);
        }
    }

    @Nested
    @DisplayName("board - 게시글 단건 조회")
    class BoardDetailTest {

        @Test
        @DisplayName("존재하는 ID로 조회하면 조회수가 증가하고 게시글을 반환한다")
        void board_존재하는ID로조회하면_조회수증가하고게시글반환한다() {
            // given
            Long boardId = 1L;
            Board board = createBoard(boardId, "테스트 제목", "작성자");

            given(boardRepository.findById(boardId))
                    .willReturn(Optional.of(board));

            // when
            Board result = boardService.board(boardId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("테스트 제목");
            assertThat(result.getViewCount()).isEqualTo(1L);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 BoardNotFoundException을 던진다")
        void board_존재하지않는ID로조회하면_예외를던진다() {
            // given
            Long invalidId = 999L;

            given(boardRepository.findById(invalidId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> boardService.board(invalidId))
                    .isInstanceOf(BoardNotFoundException.class)
                    .hasMessageContaining("999");
        }
    }

    @Nested
    @DisplayName("save - 게시글 저장")
    class SaveTest {

        @Test
        @DisplayName("정상 요청이면 게시글을 저장한다")
        void save_정상요청이면_게시글을저장한다() {
            // given
            RequestDto dto = new RequestDto();
            dto.setTitle("새 게시글");
            dto.setContent("새 게시글 내용");
            dto.setWriter("작성자");

            Page<Board> emptyPage = new PageImpl<>(List.of());

            given(boardRepository.findByTitleContaining(eq("새 게시글"), any(Pageable.class)))
                    .willReturn(emptyPage);
            given(boardRepository.save(any(Board.class)))
                    .willReturn(createBoard(1L, "새 게시글", "작성자"));

            // when
            boardService.save(dto, null);

            // then
            then(boardRepository).should().save(any(Board.class));
        }

        @Test
        @DisplayName("중복 제목이면 DuplicateTitleException을 던진다")
        void save_중복제목이면_예외를던진다() {
            // given
            RequestDto dto = new RequestDto();
            dto.setTitle("중복 제목");
            dto.setContent("내용");
            dto.setWriter("작성자");

            Board existingBoard = createBoard(1L, "중복 제목", "기존작성자");
            Page<Board> existingPage = new PageImpl<>(List.of(existingBoard));

            given(boardRepository.findByTitleContaining(eq("중복 제목"), any(Pageable.class)))
                    .willReturn(existingPage);

            // when & then
            assertThatThrownBy(() -> boardService.save(dto, null))
                    .isInstanceOf(DuplicateTitleException.class)
                    .hasMessageContaining("중복 제목");
        }
    }

    private Board createBoard(Long id, String title, String writer) {
        return Board.builder()
                .id(id)
                .title(title)
                .content(title + " 내용")
                .writer(writer)
                .build();
    }
}
