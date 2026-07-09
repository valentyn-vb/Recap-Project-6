package com.example.demo.security.jwt;

import com.example.demo.security.AuthenticatedUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    private static final String SECRET = "test-secret-test-secret-test-secret-test-secret";

    private final JwtService jwtService = new JwtService(SECRET, 3_600_000L);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);

    private final HttpServletRequest request = mock(HttpServletRequest.class);
    private final HttpServletResponse response = mock(HttpServletResponse.class);
    private final FilterChain chain = mock(FilterChain.class);

    @BeforeEach
    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validBearerTokenPopulatesSecurityContextAndContinuesChain() throws Exception {
        String token = jwtService.generateToken(42L, "alice@example.com");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        filter.doFilter(request, response, chain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo(new AuthenticatedUser(42L, "alice@example.com"));
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
        verify(chain).doFilter(request, response);
    }

    @Test
    void missingHeaderLeavesContextUnauthenticatedButContinuesChain() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(request, response);
    }

    @Test
    void nonBearerHeaderLeavesContextUnauthenticatedButContinuesChain() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(request, response);
    }

    @Test
    void invalidTokenLeavesContextUnauthenticatedButContinuesChain() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer garbage.token.value");

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(request, response);
    }
}
