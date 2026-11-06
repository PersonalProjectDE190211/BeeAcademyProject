package com.beeacademy.backend.infrastructure.auth.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA Entity ánh xạ với bảng 'refresh_tokens' trong cơ sở dữ liệu.
 */
@Entity
@Table(name = "refresh_tokens")
public class RefreshTokenJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "token_hash", nullable = false, unique = true, length = 128)
    private String tokenHash;

    @Column(name = "family_id", nullable = false)
    private UUID familyId;

    @Column(name = "replaced_by_token_hash", length = 128)
    private String replacedByTokenHash;

    @Column(name = "device_info", length = 500)
    private String deviceInfo;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked", nullable = false)
    private boolean revoked;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "revoke_reason", length = 255)
    private String revokeReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public RefreshTokenJpaEntity() {
    }

    public RefreshTokenJpaEntity(UUID id,
                                UUID userId,
                                String tokenHash,
                                UUID familyId,
                                String replacedByTokenHash,
                                String deviceInfo,
                                String ipAddress,
                                Instant expiresAt,
                                boolean revoked,
                                Instant revokedAt,
                                String revokeReason,
                                Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.familyId = familyId;
        this.replacedByTokenHash = replacedByTokenHash;
        this.deviceInfo = deviceInfo;
        this.ipAddress = ipAddress;
        this.expiresAt = expiresAt;
        this.revoked = revoked;
        this.revokedAt = revokedAt;
        this.revokeReason = revokeReason;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }

    public UUID getFamilyId() { return familyId; }
    public void setFamilyId(UUID familyId) { this.familyId = familyId; }

    public String getReplacedByTokenHash() { return replacedByTokenHash; }
    public void setReplacedByTokenHash(String replacedByTokenHash) { this.replacedByTokenHash = replacedByTokenHash; }

    public String getDeviceInfo() { return deviceInfo; }
    public void setDeviceInfo(String deviceInfo) { this.deviceInfo = deviceInfo; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public boolean isRevoked() { return revoked; }
    public void setRevoked(boolean revoked) { this.revoked = revoked; }

    public Instant getRevokedAt() { return revokedAt; }
    public void setRevokedAt(Instant revokedAt) { this.revokedAt = revokedAt; }

    public String getRevokeReason() { return revokeReason; }
    public void setRevokeReason(String revokeReason) { this.revokeReason = revokeReason; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

