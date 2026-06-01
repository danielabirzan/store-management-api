package com.danielabirzan.storemanagement.security;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilTest {

    private static final String SECRET = "test-secret-key-minimum-32-characters-long!!";

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET, 60);
    }

    @Test
    void shouldGenerateTokenAndExtractUsername() {
        String token = jwtUtil.generateToken("user", "ADMIN");

        assertThat(jwtUtil.extractUsername(token)).isEqualTo("user");
    }

    @Test
    void shouldExtractRoleFromToken() {
        String token = jwtUtil.generateToken("user", "ADMIN");

        assertThat(jwtUtil.extractRole(token)).isEqualTo("ADMIN");
    }

    @Test
    void shouldRejectExpiredToken() {
        JwtUtil expiredUtil = new JwtUtil(SECRET, -1);
        String token = expiredUtil.generateToken("user", "ADMIN");

        assertThatThrownBy(() -> expiredUtil.extractUsername(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void shouldRejectTokenWithWrongSignature() {
        String token = jwtUtil.generateToken("user", "ADMIN");

        JwtUtil otherUtil = new JwtUtil("a-completely-different-secret-key-32chars!", 60);

        assertThatThrownBy(() -> otherUtil.extractUsername(token))
                .isInstanceOf(SignatureException.class);
    }
}