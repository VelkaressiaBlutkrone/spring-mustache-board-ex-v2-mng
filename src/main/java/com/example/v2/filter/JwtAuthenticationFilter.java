package com.example.v2.filter;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.v2.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    /**
     * 요청마다 실행되는 필터 로직
     *
     * @param request     HTTP 요청
     * @param response    HTTP 응답
     * @param filterChain 필터 체인
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // 1. Authorization 헤더에서 JWT 토큰 추출
        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        // 2. Bearer 토큰 형식 확인 및 토큰 추출
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7); // "Bearer " 이후의 토큰 추출
            try {
                username = jwtUtil.extractUsername(jwt); // 토큰에서 사용자 이름 추출
            } catch (Exception e) {
                // 토큰 파싱 실패 시 로그 출력 (실제 환경에서는 로거 사용 권장)
                logger.error("JWT 토큰 파싱 실패: " + e.getMessage());
            }
        }

        // 3. 사용자 이름이 추출되었고, 현재 SecurityContext에 인증 정보가 없는 경우
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 4. 데이터베이스에서 사용자 정보 조회
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // 5. 토큰 유효성 검증
            if (jwtUtil.validateToken(jwt, userDetails)) {

                // 6. 인증 토큰 생성
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // 자격 증명 (비밀번호)은 null
                        userDetails.getAuthorities() // 권한 정보
                );

                // 7. 요청 상세 정보 설정
                authenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                // 8. SecurityContext에 인증 정보 저장
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        // 9. 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }
}
