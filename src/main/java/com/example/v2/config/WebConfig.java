package com.example.board.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 설정 클래스.
 *
 * WebMvcConfigurer 를 구현하여 Spring MVC의 기본 동작을 커스터마이징한다.
 * 여기서는 업로드된 이미지 파일을 브라우저에서 접근할 수 있도록
 * 정적 리소스 핸들러를 등록한다.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * application.properties 의 file.upload-dir 값을 주입받는다.
     * 예: ${user.dir}/uploads/
     */
    @Value("${file.upload-dir}")
    private String uploadDir;

    /** 정적 리소스 핸들러를 등록한다. */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + uploadDir);
    }
}
