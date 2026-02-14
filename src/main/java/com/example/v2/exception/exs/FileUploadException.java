package com.example.v2.exception.exs;

/**
 * 파일 업로드 처리 중 발생하는 예외.
 *
 * MultipartFile 디스크 저장 실패, Base64 디코딩 실패 등
 * 파일 업로드 과정에서 발생하는 모든 오류를 포괄한다.
 */
public class FileUploadException extends RuntimeException {

    public FileUploadException(String message) {
        super(message);
    }

    public FileUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}
