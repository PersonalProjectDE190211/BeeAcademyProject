package com.beeacademy.backend.platform.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private final JwtProperties properties = new JwtProperties(
            "test-secret-key-at-least-256-bits-long-for-hmac-sha256-testing",
            15,
            "bee-academy-test"
    );

    private final JwtService jwtService = new JwtService(properties);

    @Test
    @DisplayName("Tạo và xác thực Access Token JWT trích xuất chính xác claims")
    void testGenerateAndVerifyToken() {
        UUID userId = UUID.randomUUID();
        String email = "student@beeacademy.com";
        String role = "student";
        String fullName = "Nguyễn Văn Học";

        String token = jwtService.generateAccessToken(userId, email, role, fullName);
        assertThat(token).isNotBlank();

        Optional<DecodedJWT> decodedOpt = jwtService.verifyToken(token);
        assertThat(decodedOpt).isPresent();

        DecodedJWT jwt = decodedOpt.get();
        assertThat(jwt.getSubject()).isEqualTo(userId.toString());
        assertThat(jwt.getClaim("email").asString()).isEqualTo(email);
        assertThat(jwt.getClaim("role").asString()).isEqualTo(role);
        assertThat(jwt.getClaim("fullName").asString()).isEqualTo(fullName);

        assertThat(jwtService.extractUserId(token)).contains(userId);
        assertThat(jwtService.extractEmail(token)).contains(email);
        assertThat(jwtService.extractRole(token)).contains(role);
    }

    @Test
    @DisplayName("Token giả mạo hoặc sai định dạng trả về Optional.empty()")
    void testInvalidTokenFails() {
        assertThat(jwtService.verifyToken("invalid.token.string")).isEmpty();
        assertThat(jwtService.verifyToken("")).isEmpty();
        assertThat(jwtService.verifyToken(null)).isEmpty();
    }
}

