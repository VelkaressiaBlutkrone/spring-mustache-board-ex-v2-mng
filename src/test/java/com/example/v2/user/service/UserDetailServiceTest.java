package com.example.v2.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.example.v2.user.entity.User;
import com.example.v2.user.entity.UserDetail;
import com.example.v2.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserDetailService 단위 테스트")
class UserDetailServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailService userDetailService;

    @Test
    @DisplayName("loadUserByUsername - 존재하는 사용자명이면 UserDetail을 반환한다")
    void loadUserByUsername_존재하는사용자면_UserDetail을반환한다() {
        // given
        String username = "testuser";
        User user = User.builder()
                .id(1L)
                .username(username)
                .password("encodedPassword")
                .email("test@example.com")
                .name("테스트유저")
                .roles(Set.of("ROLE_USER"))
                .enabled(true)
                .build();

        given(userRepository.findByUsername(username))
                .willReturn(Optional.of(user));

        // when
        UserDetails result = userDetailService.loadUserByUsername(username);

        // then
        assertThat(result).isNotNull().isInstanceOf(UserDetail.class);
        assertThat(result.getUsername()).isEqualTo(username);
        assertThat(result.getPassword()).isEqualTo("encodedPassword");
        assertThat(result.getAuthorities()).hasSize(1);
        assertThat(result.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("loadUserByUsername - 존재하지 않는 사용자명이면 UsernameNotFoundException을 던진다")
    void loadUserByUsername_존재하지않는사용자면_예외를던진다() {
        // given
        String username = "unknown";

        given(userRepository.findByUsername(username))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userDetailService.loadUserByUsername(username))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("unknown");
    }

    @Test
    @DisplayName("loadUserByUsername - 여러 권한을 가진 사용자면 모든 권한을 반환한다")
    void loadUserByUsername_여러권한사용자면_모든권한을반환한다() {
        // given
        String username = "admin";
        User user = User.builder()
                .id(1L)
                .username(username)
                .password("encodedPassword")
                .email("admin@example.com")
                .name("관리자")
                .roles(Set.of("ROLE_USER", "ROLE_ADMIN"))
                .enabled(true)
                .build();

        given(userRepository.findByUsername(username))
                .willReturn(Optional.of(user));

        // when
        UserDetails result = userDetailService.loadUserByUsername(username);

        // then
        assertThat(result.getAuthorities()).hasSize(2);
        assertThat(result.getAuthorities())
                .extracting("authority")
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    @DisplayName("loadUserByUsername - 비활성화된 사용자면 isEnabled가 false를 반환한다")
    void loadUserByUsername_비활성화사용자면_isEnabled가false를반환한다() {
        // given
        String username = "disabled";
        User user = User.builder()
                .id(1L)
                .username(username)
                .password("encodedPassword")
                .email("disabled@example.com")
                .name("비활성유저")
                .roles(Set.of("ROLE_USER"))
                .enabled(false)
                .build();

        given(userRepository.findByUsername(username))
                .willReturn(Optional.of(user));

        // when
        UserDetails result = userDetailService.loadUserByUsername(username);

        // then
        assertThat(result.isEnabled()).isFalse();
    }
}
