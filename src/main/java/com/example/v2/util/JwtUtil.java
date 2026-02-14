package com.example.v2.util;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    // application.yml에서 주입받는 비밀 키
    @Value("${jwt.secret}")
    private String secretKey;

    // 토큰 만료 시간 (밀리초)
    @Value("${jwt.expiration}")
    private Long expiration;

    // 리프레시 토큰 만료 시간 (밀리초)
    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    /**
     * 비밀 키를 SecretKey 객체로 변환
     *
     * @return SecretKey 객체
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 토큰에서 사용자 이름(username) 추출
     *
     * @param token JWT 토큰
     * @return 사용자 이름
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * 토큰에서 만료 시간 추출
     *
     * @param token JWT 토큰
     * @return 만료 시간
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * 토큰에서 특정 클레임 추출
     *
     * @param token          JWT 토큰
     * @param claimsResolver 클레임 추출 함수
     * @param <T>            반환 타입
     * @return 추출된 클레임
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 토큰에서 모든 클레임 추출
     *
     * @param token JWT 토큰
     * @return Claims 객체
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey()) // 서명 검증을 위한 키 설정
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 토큰 만료 여부 확인
     *
     * @param token JWT 토큰
     * @return 만료되었으면 true, 아니면 false
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * 사용자 정보를 기반으로 액세스 토큰 생성
     *
     * @param userDetails 사용자 상세 정보
     * @return 생성된 JWT 토큰
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // 추가 클레임이 필요한 경우 여기에 추가
        // claims.put("role", userDetails.getAuthorities());
        return createToken(claims, userDetails.getUsername(), expiration);
    }

    /**
     * 리프레시 토큰 생성
     *
     * @param userDetails 사용자 상세 정보
     * @return 생성된 리프레시 토큰
     */
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername(), refreshExpiration);
    }

    /**
     * JWT 토큰 생성 로직
     *
     * @param claims         추가할 클레임
     * @param subject        토큰 주체 (사용자 이름)
     * @param expirationTime 만료 시간
     * @return 생성된 JWT 토큰
     */
    private String createToken(Map<String, Object> claims, String subject, Long expirationTime) {
        return Jwts.builder()
                .claims(claims) // 커스텀 클레임 추가
                .subject(subject) // 토큰 주체 설정 (사용자 이름)
                .issuedAt(new Date(System.currentTimeMillis())) // 발급 시간
                .expiration(new Date(System.currentTimeMillis() + expirationTime)) // 만료 시간
                .signWith(getSigningKey()) // 서명 키로 서명
                .compact(); // 토큰 생성
    }

    /**
     * 토큰 유효성 검증
     *
     * @param token       JWT 토큰
     * @param userDetails 사용자 상세 정보
     * @return 유효하면 true, 아니면 false
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        // 토큰의 사용자 이름이 일치하고 만료되지 않았는지 확인
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * 토큰 검증 (UserDetails 없이)
     *
     * @param token JWT 토큰
     * @return 유효하면 true, 아니면 false
     */
    public Boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
