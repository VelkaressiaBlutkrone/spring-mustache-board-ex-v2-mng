package com.example.v2.exception.exs;

/**
 * 업로드된 파일을 서버에서 찾을 수 없을 때 발생하는 예외.
 *
 * 업로드 디렉토리에 해당 파일이 존재하지 않거나,
 * 원본 파일명이 누락되어 확장자를 추출할 수 없을 때 발생한다.
 */
public class FileNotFoundException extends RuntimeException {

    public FileNotFoundException(String message) {
        super(message);
    }

    public FileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
