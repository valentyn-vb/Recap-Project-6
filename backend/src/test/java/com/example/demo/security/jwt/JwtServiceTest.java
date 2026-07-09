package com.example.demo.security.jwt;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "test-secret-test-secret-test-secret-test-secret";
    private static final long ONE_HOUR_MS = 3_600_000L;

    @Test
    void generatedTokenRoundTripsUserIdAndEmail() {
        JwtService jwtService = new JwtService(SECRET, ONE_HOUR_MS);

        String token = jwtService.generateToken(42L, "alice@example.com");

        assertThat(jwtService.isValid(token)).isTrue();
        assertThat(jwtService.extractUserId(token)).isEqualTo(42L);
        assertThat(jwtService.extractEmail(token)).isEqualTo("alice@example.com");
    }

    @Test
    void tokenSignedWithDifferentSecretIsRejected() {
        JwtService issuer = new JwtService("another-secret-another-secret-another-secret", ONE_HOUR_MS);
        JwtService verifier = new JwtService(SECRET, ONE_HOUR_MS);

        String foreignToken = issuer.generateToken(42L, "alice@example.com");

        assertThat(verifier.isValid(foreignToken)).isFalse();
    }

    @Test
    void expiredTokenIsRejected() {
        JwtService jwtService = new JwtService(SECRET, -1_000L);

        String expiredToken = jwtService.generateToken(42L, "alice@example.com");

        assertThat(jwtService.isValid(expiredToken)).isFalse();
    }

    @Test
    void garbageTokenIsRejected() {
        JwtService jwtService = new JwtService(SECRET, ONE_HOUR_MS);

        assertThat(jwtService.isValid("not-a-jwt-at-all")).isFalse();
    }

    @Test
    void tooShortSecretFailsFastAtConstruction() {
        assertThatThrownBy(() -> new JwtService("short", ONE_HOUR_MS))
                .isInstanceOf(IllegalStateException.class);
    }
}
