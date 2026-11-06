package com.beeacademy.backend.platform.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * Service quản lý ký và xác thực Access Token JWT chuẩn HMAC-SHA256 (Native Auth).
 */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final JwtProperties properties;
    private final Algorithm algorithm;
    private final JWTVerifier verifier;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.algorithm = Algorithm.HMAC256(properties.secret());
        this.verifier = JWT.require(this.algorithm)
                .withIssuer(properties.issuer())
                .build();
    }

    public String generateAccessToken(UUID userId, String email, String role, String fullName) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(properties.accessTokenExpirationMinutes(), ChronoUnit.MINUTES);

        return JWT.create()
                .withIssuer(properties.issuer())
                .withSubject(userId.toString())
                .withClaim("userId", userId.toString())
                .withClaim("email", email)
                .withClaim("role", role == null ? "student" : role.toLowerCase())
                .withClaim("fullName", fullName == null ? "" : fullName)
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(expiresAt))
                .sign(algorithm);
    }

    public String generateAccessToken(UUID userId, String email, String role) {
        return generateAccessToken(userId, email, role, "");
    }

    public Optional<DecodedJWT> verifyToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        try {
            DecodedJWT decoded = verifier.verify(token);
            return Optional.of(decoded);
        } catch (JWTVerificationException ex) {
            log.warn("Xác thực JWT thất bại: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    public Optional<UUID> extractUserId(String token) {
        return verifyToken(token).map(jwt -> {
            try {
                return UUID.fromString(jwt.getSubject());
            } catch (Exception e) {
                return null;
            }
        });
    }

    public Optional<String> extractRole(String token) {
        return verifyToken(token).map(jwt -> jwt.getClaim("role").asString());
    }

    public Optional<String> extractEmail(String token) {
        return verifyToken(token).map(jwt -> jwt.getClaim("email").asString());
    }
}

