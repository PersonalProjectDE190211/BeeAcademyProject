package com.beeacademy.backend.platform.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String secret,
        long accessTokenExpirationMinutes,
        String issuer
) {
    public JwtProperties {
        if (secret == null || secret.isBlank()) {
            secret = "bee-academy-default-jwt-secret-key-must-be-at-least-256-bits-long-for-hmac-sha256";
        }
        if (accessTokenExpirationMinutes <= 0) {
            accessTokenExpirationMinutes = 15; // Mặc định 15 phút
        }
        if (issuer == null || issuer.isBlank()) {
            issuer = "bee-academy";
        }
    }
}

