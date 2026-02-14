package com.example.v2.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.example.v2.exception.exs.BoardNotFoundException;
import com.example.v2.exception.exs.DuplicateTitleException;
import com.example.v2.exception.exs.FileNotFoundException;
import com.example.v2.exception.exs.FileUploadException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * 전역 예외 처리기.
 *
 * @ControllerAdvice 로 모든 컨트롤러에서 발생하는 예외를 공통으로 처리한다.
 *                   - SSR 요청 (/board/**) : 에러 페이지(Mustache 템플릿)를 반환한다.
 *                   - REST API 요청 (/board/api/**) : JSON 에러 응답을 반환한다.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /** 게시글을 찾을 수 없을 때 (404) */
    @ExceptionHandler(BoardNotFoundException.class)
    public Object handleBoardNotFound(BoardNotFoundException ex, HttpServletRequest req) {
        log.warn("BoardNotFoundException: {}", ex.getMessage());
        if (isApiRequest(req)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", ex.getMessage()));
        }
        req.setAttribute("status", "404 Not Found");
        req.setAttribute("message", ex.getMessage());
        return "error";
    }

    /** 파일 업로드 실패 시 (500) */
    @ExceptionHandler(FileUploadException.class)
    public Object handleFileUpload(FileUploadException ex, HttpServletRequest req) {
        log.error("FileUploadException: {}", ex.getMessage(), ex);
        if (isApiRequest(req)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", ex.getMessage()));
        }
        req.setAttribute("status", "500 Internal Server Error");
        req.setAttribute("message", ex.getMessage());
        return "error";
    }

    /** 업로드 파일을 찾을 수 없을 때 (404) */
    @ExceptionHandler(FileNotFoundException.class)
    public Object handleFileNotFound(FileNotFoundException ex, HttpServletRequest req) {
        log.warn("FileNotFoundException: {}", ex.getMessage());
        if (isApiRequest(req)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", ex.getMessage()));
        }
        req.setAttribute("status", "404 Not Found");
        req.setAttribute("message", ex.getMessage());
        return "error";
    }

    /** 중복 제목으로 저장 시도 시 (409) */
    @ExceptionHandler(DuplicateTitleException.class)
    public Object handleDuplicateTitle(DuplicateTitleException ex, HttpServletRequest req) {
        log.warn("DuplicateTitleException: {}", ex.getMessage());
        if (isApiRequest(req)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", ex.getMessage()));
        }
        req.setAttribute("status", "409 Conflict");
        req.setAttribute("message", ex.getMessage());
        return "error";
    }

    /** 기타 예상치 못한 예외 (500) */
    @ExceptionHandler(Exception.class)
    public Object handleException(Exception ex, HttpServletRequest req) {
        log.error("Unhandled Exception: {}", ex.getMessage(), ex);
        if (isApiRequest(req)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "서버 내부 오류가 발생했습니다."));
        }
        req.setAttribute("status", "500 Internal Server Error");
        req.setAttribute("message", "서버 내부 오류가 발생했습니다.");
        return "error";
    }

    /** 요청 URI 가 /board/api/ 로 시작하면 REST API 요청으로 판별한다. */
    private boolean isApiRequest(HttpServletRequest req) {
        return req.getRequestURI().startsWith("/board/api");
    }
}
