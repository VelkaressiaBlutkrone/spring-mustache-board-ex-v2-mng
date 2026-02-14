package com.example.board;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 애플리케이션 진입점.
 *
 * @SpringBootApplication 은 아래 3개 어노테이션을 포함하는 복합 어노테이션이다.
 * - @Configuration       : 이 클래스를 Spring 설정 클래스로 등록
 * - @EnableAutoConfiguration : 클래스패스의 라이브러리를 감지하여 자동 설정 적용
 * - @ComponentScan       : 현재 패키지(com.example.board) 하위의 모든 컴포넌트를 스캔
 */
@SpringBootApplication
public class SpringMustacheBoardExampleApplication {

	/** 애플리케이션 진입점. */
	public static void main(String[] args) {
		SpringApplication.run(SpringMustacheBoardExampleApplication.class, args);
	}

}
