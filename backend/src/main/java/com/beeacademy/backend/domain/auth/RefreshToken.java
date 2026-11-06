package com.beeacademy.backend.domain.auth;

import com.beeacademy.backend.domain.common.AbstractAggregateRoot;
import com.beeacademy.backend.domain.common.DomainException;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Entity đại diện cho Refresh Token trong cơ chế Native JWT Authentication.
 * Quản lý vòng đời phiên đăng nhập an toàn theo chuẩn RFC 6749 & OWASP:
 * - Chỉ lưu bản băm SHA-256 (tokenHash), tuyệt đối không lưu raw token ở Database.
 * - Hỗ trợ Refresh Token Rotation: Mỗi lần cấp Access Token mới, Refresh Token cũ sẽ bị hủy (revoked)
 *   và lưu vết mã băm token thay thế (replacedByTokenHash).
 * - Phát hiện hành vi chiếm đoạt phiên (Token Reuse Detection): Nếu một token đã bị revoke mà vẫn được gửi lên,
 *   hệ thống sẽ phát hiện bị lộ và thu hồi toàn bộ các phiên khác của người dùng đó.
 */
public class RefreshToken extends AbstractAggregateRoot {

    private final UUID id;
    private final UUID userId;
    private final String tokenHash;
    private final UUID familyId;
    private final Instant expiresAt;
    private final Instant createdAt;
    private Instant revokedAt;
    private String replacedByTokenHash;

    protected RefreshToken(UUID id,
                           UUID userId,
                           String tokenHash,
                           UUID familyId,
                           Instant expiresAt,
                           Instant createdAt,
                           Instant revokedAt,
                           String replacedByTokenHash) {
        this.id = id;
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.familyId = familyId != null ? familyId : UUID.randomUUID();
        this.expiresAt = expiresAt;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.revokedAt = revokedAt;
        this.replacedByTokenHash = replacedByTokenHash;
    }

    /**
     * Cấp mới một Refresh Token cho người dùng khi đăng nhập.
     *
     * @param userId    ID của người dùng
     * @param tokenHash Bản băm SHA-256 (64 hex characters) của raw refresh token
     * @param ttl       Thời gian sống của token (ví dụ: 7 ngày, 30 ngày)
     */
    public static RefreshToken issue(UUID userId, String tokenHash, Duration ttl) {
        return issue(userId, tokenHash, ttl, UUID.randomUUID());
    }

    /**
     * Cấp mới một Refresh Token trong cùng một phiên Token Family (khi xoay vòng).
     */
    public static RefreshToken issue(UUID userId, String tokenHash, Duration ttl, UUID familyId) {
        if (userId == null) {
            throw new DomainException("INVALID_USER_ID", "User ID không được null");
        }
        if (tokenHash == null || tokenHash.isBlank() || tokenHash.length() != 64) {
            throw new DomainException("INVALID_TOKEN_HASH", "Token hash phải là chuỗi SHA-256 hợp lệ 64 ký tự");
        }
        if (ttl == null || ttl.isNegative() || ttl.isZero()) {
            throw new DomainException("INVALID_TTL", "Thời gian sống của token không hợp lệ");
        }

        Instant now = Instant.now();
        return new RefreshToken(
                UUID.randomUUID(),
                userId,
                tokenHash,
                familyId != null ? familyId : UUID.randomUUID(),
                now.plus(ttl),
                now,
                null,
                null
        );
    }

    /**
     * Tái tạo Entity từ Database.
     */
    public static RefreshToken rehydrate(UUID id,
                                         UUID userId,
                                         String tokenHash,
                                         Instant expiresAt,
                                         Instant createdAt,
                                         Instant revokedAt,
                                         String replacedByTokenHash) {
        return new RefreshToken(id, userId, tokenHash, UUID.randomUUID(), expiresAt, createdAt, revokedAt, replacedByTokenHash);
    }

    public static RefreshToken rehydrate(UUID id,
                                         UUID userId,
                                         String tokenHash,
                                         UUID familyId,
                                         Instant expiresAt,
                                         Instant createdAt,
                                         Instant revokedAt,
                                         String replacedByTokenHash) {
        return new RefreshToken(id, userId, tokenHash, familyId, expiresAt, createdAt, revokedAt, replacedByTokenHash);
    }

    /**
     * Kiểm tra token đã hết hạn hay chưa.
     */
    public boolean isExpired(Instant now) {
        return now.isAfter(this.expiresAt);
    }

    /**
     * Kiểm tra token đã bị thu hồi (revoked) hay chưa.
     */
    public boolean isRevoked() {
        return this.revokedAt != null;
    }

    /**
     * Kiểm tra token có hợp lệ để sử dụng hay không (chưa hết hạn và chưa bị thu hồi).
     */
    public boolean isValid(Instant now) {
        return !isRevoked() && !isExpired(now);
    }

    /**
     * Thực hiện xoay vòng token (Refresh Token Rotation):
     * Đánh dấu token hiện tại bị thu hồi và liên kết với token mới.
     */
    public void rotate(String newTokenHash, Instant now) {
        if (isRevoked()) {
            throw new DomainException("TOKEN_ALREADY_REVOKED", "Token này đã bị thu hồi trước đó");
        }
        if (isExpired(now)) {
            throw new DomainException("TOKEN_EXPIRED", "Token này đã hết hạn, không thể xoay vòng");
        }
        if (newTokenHash == null || newTokenHash.isBlank() || newTokenHash.length() != 64) {
            throw new DomainException("INVALID_TOKEN_HASH", "Mã băm token mới không hợp lệ");
        }

        this.revokedAt = now;
        this.replacedByTokenHash = newTokenHash;
    }

    /**
     * Thu hồi chủ động (người dùng bấm Đăng xuất).
     */
    public void revoke(Instant now) {
        if (!isRevoked()) {
            this.revokedAt = now;
        }
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getTokenHash() { return tokenHash; }
    public UUID getFamilyId() { return familyId; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getRevokedAt() { return revokedAt; }
    public String getReplacedByTokenHash() { return replacedByTokenHash; }
}

