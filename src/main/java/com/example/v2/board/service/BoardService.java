package com.example.v2.board.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.v2.board.dto.Base64Dto;
import com.example.v2.board.dto.RequestDto;
import com.example.v2.board.entity.Board;
import com.example.v2.board.exception.exs.BoardNotFoundException;
import com.example.v2.board.exception.exs.DuplicateTitleException;
import com.example.v2.board.exception.exs.FileUploadException;
import com.example.v2.board.repository.BoardRepository;

import lombok.RequiredArgsConstructor;

/**
 * 게시판 비즈니스 로직 서비스.
 *
 * 게시글 CRUD 및 파일 업로드 처리를 담당한다.
 *
 * @RequiredArgsConstructor 로 final 필드(BoardRepository)의 생성자 주입을 자동 생성한다.
 */
@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository repository;

    /** application.properties 의 file.upload-dir 값 (파일 업로드 경로) */
    @Value("${file.upload-dir}")
    private String uploadDir;

    /** 게시글 목록을 조회한다. */
    public Page<Board> boardList(String searchType, String keyword, Pageable pageable) {
        Page<Board> boards;
        if (keyword == null || keyword.isBlank()) {
            // 키워드가 없으면 전체 목록 조회
            boards = repository.findAllByOrderByCreatedAtDesc(pageable);
        } else {
            // 검색 유형에 따른 조건 분기 (Java 14+ switch expression)
            boards = switch (searchType) {
                case "title" -> repository.findByTitleContaining(keyword, pageable);
                case "writer" -> repository.findByWriterContaining(keyword, pageable);
                case "titleContent" -> repository.findByTitleContainingOrContentContaining(keyword, keyword, pageable);
                default -> repository.findAllByOrderByCreatedAtDesc(pageable);
            };
        }
        return Optional.ofNullable(boards).orElseThrow();
    }

    /** 게시글 단건 조회 및 조회수 증가. */
    @Transactional
    public Board board(Long id) {
        Board board = repository.findById(id)
                .orElseThrow(() -> new BoardNotFoundException(id));
        board.increaseViewCount();
        return board;
    }

    /** 게시글을 저장한다 (폼 전송 + MultipartFile 업로드). */
    @Transactional
    public Optional<Void> save(RequestDto dto, MultipartFile file) {
        // 중복 제목 검증
        if (repository.findByTitleContaining(dto.getTitle(), Pageable.unpaged()).hasContent()) {
            // 정확히 동일한 제목이 존재하는지 확인
            repository.findByTitleContaining(dto.getTitle(), Pageable.unpaged())
                    .getContent().stream()
                    .filter(b -> b.getTitle().equals(dto.getTitle()))
                    .findFirst()
                    .ifPresent(b -> {
                        throw new DuplicateTitleException(dto.getTitle());
                    });
        }

        Board board = dto.toEntity();

        // 첨부파일이 존재하면 업로드 처리
        if (file != null && !file.isEmpty()) {
            FileUploadResult result = fileUpload(file);
            board.setFileInfo(result.filename, result.originalFileName);
        }

        repository.save(board);
        return Optional.empty();
    }

    /** MultipartFile을 디스크에 저장한다. */
    public FileUploadResult fileUpload(MultipartFile file) {
        // 원본 파일명에서 확장자 추출
        String original = file.getOriginalFilename();
        if (original == null || !original.contains(".")) {
            throw new FileUploadException("파일명이 없거나 확장자가 누락되었습니다.");
        }
        String ext = original.substring(original.lastIndexOf("."));
        // UUID + 확장자로 저장 파일명 생성 (예: "550e8400-e29b-41d4-a716-446655440000.png")
        String saved = UUID.randomUUID() + ext;

        // 업로드 디렉토리가 없으면 생성
        File dir = new File(uploadDir);
        if (!dir.exists())
            dir.mkdirs();

        // 파일을 디스크에 저장
        try {
            file.transferTo(new File(dir, saved));
        } catch (IllegalStateException | IOException e) {
            throw new FileUploadException("파일 저장에 실패했습니다. (" + original + ")", e);
        }

        return new FileUploadResult(saved, original);
    }

    /** 게시글을 저장한다 (REST API + Base64 이미지 업로드). */
    @Transactional
    public Optional<Void> save2(Base64Dto dto) {
        // 중복 제목 검증
        repository.findByTitleContaining(dto.getTitle(), Pageable.unpaged())
                .getContent().stream()
                .filter(b -> b.getTitle().equals(dto.getTitle()))
                .findFirst()
                .ifPresent(b -> {
                    throw new DuplicateTitleException(dto.getTitle());
                });

        Board board = dto.toEntity();
        FileUploadResult result = base64FileUpload(dto.getImageBase64(), dto.getImageFileName());
        board.setFileInfo(result.filename, result.originalFileName);

        repository.save(board);
        return Optional.empty();
    }

    /** Base64 인코딩된 이미지를 디코딩하여 파일로 저장한다. */
    public FileUploadResult base64FileUpload(String base64File, String originalFileName) {
        if (base64File == null || base64File.isEmpty()) {
            return new FileUploadResult("", "");
        }

        if (originalFileName == null || !originalFileName.contains(".")) {
            throw new FileUploadException("원본 파일명이 없거나 확장자가 누락되었습니다.");
        }

        // "data:image/png;base64,AAAA..." 형식에서 "," 뒤의 순수 Base64 데이터만 추출
        String pureBase64File = base64File;
        if (base64File.contains(",")) {
            pureBase64File = base64File.substring(base64File.indexOf(",") + 1);
        }

        // Base64 디코딩 → byte 배열 변환
        byte[] imageByte;
        try {
            imageByte = Base64.getDecoder().decode(pureBase64File);
        } catch (IllegalArgumentException e) {
            throw new FileUploadException("Base64 디코딩에 실패했습니다.", e);
        }

        // 원본 파일명에서 확장자 추출 후 UUID 파일명 생성
        String ext = originalFileName.substring(originalFileName.lastIndexOf("."));
        String fileName = UUID.randomUUID() + ext;

        // 업로드 디렉토리가 없으면 생성
        File dir = new File(uploadDir);
        if (!dir.exists())
            dir.mkdir();

        // byte 배열을 파일로 저장
        try {
            Files.write(Path.of(uploadDir, fileName), imageByte);
        } catch (IOException e) {
            throw new FileUploadException("Base64 이미지 저장에 실패했습니다. (" + originalFileName + ")", e);
        }

        return new FileUploadResult(fileName, originalFileName);
    }

    /**
     * 파일 업로드 결과를 담는 레코드.
     *
     * @param filename         서버에 저장된 파일명 (UUID + 확장자)
     * @param originalFileName 사용자가 업로드한 원본 파일명
     */
    public record FileUploadResult(String filename, String originalFileName) {

    }

}
