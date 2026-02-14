package com.example.v2.board.controller;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.v2.board.dto.Base64Dto;
import com.example.v2.board.service.BoardService;

import lombok.RequiredArgsConstructor;

/**
 * 게시판 REST API 컨트롤러.
 *
 * @RestController 는 @Controller + @ResponseBody 의 복합 어노테이션으로,
 *                 메서드 반환값이 뷰 이름이 아닌 HTTP 응답 본문으로 직접 직렬화된다.
 *
 *                 클라이언트의 Fetch API 를 통해 JSON 형태로 게시글 데이터(Base64 이미지 포함)를
 *                 전송받아 처리한다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/board/api")
public class BoardRestController {

    private final BoardService boardService;

    /** 게시글을 저장하고 303 리다이렉트 응답을 반환한다. */
    @PostMapping("/write")
    public ResponseEntity<Void> write(@RequestBody Base64Dto dto) {
        boardService.save2(dto);

        // 303 See Other: POST 후 GET 리다이렉트를 유도하는 HTTP 상태 코드
        return ResponseEntity.status(HttpStatus.SEE_OTHER)
                .location(URI.create("/"))
                .build();
    }
}
