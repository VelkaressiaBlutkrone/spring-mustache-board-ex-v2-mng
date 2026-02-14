package com.example.v2.board.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.v2.board.dto.PagingDto;
import com.example.v2.board.dto.RequestDto;
import com.example.v2.board.dto.ResponseDto;
import com.example.v2.board.entity.Board;
import com.example.v2.board.service.BoardService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * 게시판 페이지 컨트롤러 (SSR - Server Side Rendering).
 *
 * @Controller 어노테이션으로 뷰 이름(Mustache 템플릿 경로)을 반환한다.
 *             @RequestMapping("/board") 로 모든 핸들러의 공통 경로를 /board 로 설정한다.
 * @RequiredArgsConstructor 로 final 필드(BoardService)의 생성자 주입을 자동 생성한다.
 */
@Controller
@RequiredArgsConstructor
public class BoardController {

    private final BoardService service;

    /** 게시글 목록 페이지를 조회한다. */
    @GetMapping("/")
    public String listPage(@PageableDefault(size = 10, sort = "id", direction = Direction.DESC) Pageable pageable,
            @RequestParam(required = false, defaultValue = "") String searchType,
            @RequestParam(required = false, defaultValue = "") String keyword, HttpServletRequest req) {
        // 서비스에서 페이징된 게시글 조회
        Page<Board> boards = service.boardList(searchType, keyword, pageable);
        // Board 엔티티 → ResponseDto 변환 (날짜 포맷팅 등 뷰에 맞게 가공)
        List<ResponseDto> dtos = boards.getContent().stream().map(b -> new ResponseDto(b)).toList();

        // Mustache 템플릿에 전달할 데이터를 request attribute 에 세팅
        req.setAttribute("boards", dtos); // 게시글 목록
        req.setAttribute("totalElements", boards.getTotalElements()); // 전체 게시글 수
        req.setAttribute("paging", new PagingDto(boards, searchType, keyword)); // 페이징 네비게이션 정보
        req.setAttribute("searchType", searchType); // 검색 유형 (검색폼 복원용)
        req.setAttribute("keyword", keyword); // 검색 키워드 (검색폼 복원용)
        return "/board/list";
    }

    /** 게시글 상세 페이지를 조회한다. */
    @GetMapping("/board/detail/{id}")
    public String detailPage(@PathVariable("id") Long id, HttpServletRequest req) {
        Board board = service.board(id);
        // Board 엔티티 → ResponseDto 변환
        ResponseDto dto = new ResponseDto(board);
        req.setAttribute("board", dto);
        return "/board/detail";
    }

    /** 글쓰기 페이지를 렌더링한다. */
    @GetMapping("/board/write")
    public String writePage(HttpServletRequest req) {
        return "/board/write";
    }

    /** 게시글을 저장한다. */
    @PostMapping("/board/write")
    public String save(@ModelAttribute RequestDto dto, @RequestParam(required = false) MultipartFile file) {
        service.save(dto, file);
        return "redirect:/";
    }
}
