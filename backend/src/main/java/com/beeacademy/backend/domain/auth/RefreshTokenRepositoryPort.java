package com.beeacademy.backend.domain.auth;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain Repository Port cho Aggregate {@link RefreshToken}.
 */
public interface RefreshTokenRepositoryPort {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    RefreshToken save(RefreshToken refreshToken);

    /**
     * Thu hồi toàn bộ Refresh Tokens của một người dùng (khi đổi mật khẩu hoặc phát hiện Token bị lộ).
     */
    void revokeAllByUserId(UUID userId, Instant now);

    /**
     * Dọn dẹp các token đã hết hạn khỏi database.
     */
    void deleteExpiredTokens(Instant now);
}
