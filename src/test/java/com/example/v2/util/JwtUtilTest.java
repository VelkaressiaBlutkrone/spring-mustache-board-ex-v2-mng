package com.example.v2.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.v2.user.entity.User;
import com.example.v2.user.entity.UserDetail;

@DisplayName("JwtUtil 단위 테스트")
class JwtUtilTest {

    private JwtUtil jwtUtil;

    private static final String TEST_SECRET = "a347d448b111a6ae5212cccc43b29c4fbfeffa0e490279a17115d33c910247d2";
    private static final Long TEST_EXPIRATION = 86400000L;
    private static final Long TEST_REFRESH_EXPIRATION = 604800000L;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secretKey", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", TEST_EXPIRATION);
        ReflectionTestUtils.setField(jwtUtil, "refreshExpiration", TEST_REFRESH_EXPIRATION);
    }

    private UserDetail createUserDetail(String username) {
        User user = User.builder()
                .id(1L)
                .username(username)
                .password("encodedPassword")
                .email(username + "@example.com")
                .roles(Set.of("ROLE_USER"))
                .enabled(true)
                .build();
        return new UserDetail(user);
    }

    @Nested
    @DisplayName("generateToken - 토큰 생성")
    class GenerateTokenTest {

        @Test
        @DisplayName("유효한 사용자 정보면 토큰을 생성한다")
        void generateToken_유효한사용자정보면_토큰을생성한다() {
            // given
            UserDetail userDetail = createUserDetail("testuser");

            // when
            String token = jwtUtil.generateToken(userDetail);

            // then
            assertThat(token).isNotNull().isNotBlank();
            assertThat(token.split("\\.")).hasSize(3);
        }

        @Test
        @DisplayName("리프레시 토큰도 정상 생성된다")
        void generateRefreshToken_유효한사용자정보면_리프레시토큰을생성한다() {
            // given
            UserDetail userDetail = createUserDetail("testuser");

            // when
            String refreshToken = jwtUtil.generateRefreshToken(userDetail);

            // then
            assertThat(refreshToken).isNotNull().isNotBlank();
            assertThat(refreshToken.split("\\.")).hasSize(3);
        }
    }

    @Nested
    @DisplayName("extractUsername - 사용자 이름 추출")
    class ExtractUsernameTest {

        @Test
        @DisplayName("유효한 토큰이면 사용자 이름을 반환한다")
        void extractUsername_유효한토큰이면_사용자이름을반환한다() {
            // given
            String username = "testuser";
            UserDetail userDetail = createUserDetail(username);
            String token = jwtUtil.generateToken(userDetail);

            // when
            String extractedUsername = jwtUtil.extractUsername(token);

            // then
            assertThat(extractedUsername).isEqualTo(username);
        }

        @Test
        @DisplayName("잘못된 토큰이면 예외를 던진다")
        void extractUsername_잘못된토큰이면_예외를던진다() {
            // given
            String invalidToken = "invalid.token.value";

            // when & then
            assertThatThrownBy(() -> jwtUtil.extractUsername(invalidToken))
                    .isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("validateToken - 토큰 유효성 검증")
    class ValidateTokenTest {

        @Test
        @DisplayName("유효한 토큰과 일치하는 사용자면 true를 반환한다")
        void validateToken_유효한토큰과사용자면_true를반환한다() {
            // given
            UserDetail userDetail = createUserDetail("testuser");
            String token = jwtUtil.generateToken(userDetail);

            // when
            Boolean isValid = jwtUtil.validateToken(token, userDetail);

            // then
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("토큰의 사용자와 다른 사용자면 false를 반환한다")
        void validateToken_다른사용자면_false를반환한다() {
            // given
            UserDetail tokenUser = createUserDetail("user1");
            UserDetail otherUser = createUserDetail("user2");
            String token = jwtUtil.generateToken(tokenUser);

            // when
            Boolean isValid = jwtUtil.validateToken(token, otherUser);

            // then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("만료된 토큰이면 false를 반환한다")
        void validateToken_만료된토큰이면_false를반환한다() {
            // given
            JwtUtil expiredJwtUtil = new JwtUtil();
            ReflectionTestUtils.setField(expiredJwtUtil, "secretKey", TEST_SECRET);
            ReflectionTestUtils.setField(expiredJwtUtil, "expiration", -1000L);
            ReflectionTestUtils.setField(expiredJwtUtil, "refreshExpiration", TEST_REFRESH_EXPIRATION);

            UserDetail userDetail = createUserDetail("testuser");
            String expiredToken = expiredJwtUtil.generateToken(userDetail);

            // when
            Boolean isValid = jwtUtil.validateToken(expiredToken);

            // then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("UserDetails 없이 유효한 토큰이면 true를 반환한다")
        void validateToken_UserDetails없이유효한토큰이면_true를반환한다() {
            // given
            UserDetail userDetail = createUserDetail("testuser");
            String token = jwtUtil.generateToken(userDetail);

            // when
            Boolean isValid = jwtUtil.validateToken(token);

            // then
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("UserDetails 없이 잘못된 토큰이면 false를 반환한다")
        void validateToken_UserDetails없이잘못된토큰이면_false를반환한다() {
            // given
            String invalidToken = "invalid.jwt.token";

            // when
            Boolean isValid = jwtUtil.validateToken(invalidToken);

            // then
            assertThat(isValid).isFalse();
        }
    }

    @Nested
    @DisplayName("extractExpiration - 만료 시간 추출")
    class ExtractExpirationTest {

        @Test
        @DisplayName("유효한 토큰이면 만료 시간을 반환한다")
        void extractExpiration_유효한토큰이면_만료시간을반환한다() {
            // given
            UserDetail userDetail = createUserDetail("testuser");
            String token = jwtUtil.generateToken(userDetail);

            // when
            var expiration = jwtUtil.extractExpiration(token);

            // then
            assertThat(expiration).isNotNull();
            assertThat(expiration.getTime())
                    .isGreaterThan(System.currentTimeMillis());
        }
    }
}
