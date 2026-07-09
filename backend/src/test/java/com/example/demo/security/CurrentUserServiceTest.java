package com.example.demo.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CurrentUserServiceTest {

    private final CurrentUserService currentUserService = new CurrentUserService();

    @BeforeEach
    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsUserIdOfAuthenticatedPrincipal() {
        AuthenticatedUser principal = new AuthenticatedUser(42L, "alice@example.com");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        assertThat(currentUserService.getCurrentUserId()).isEqualTo(42L);
    }

    @Test
    void throwsWhenNoAuthenticationPresent() {
        assertThatThrownBy(currentUserService::getCurrentUserId)
                .isInstanceOf(IllegalStateException.class);
    }
}
