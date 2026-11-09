package com.beeacademy.backend.domain.auth;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain Entity đại diện cho liên kết tài khoản định danh mạng xã hội (Google, Apple, Facebook...).
 * Định danh duy nhất thông qua cặp (provider, providerUserId - tức claim 'sub' của Google).
 */
public class ExternalLogin {

    private final UUID id;
    private final UUID userId;
    private final String provider;
    private final String providerUserId;
    private final String email;
    private final Instant createdAt;

    public ExternalLogin(UUID id, UUID userId, String provider, String providerUserId, String email, Instant createdAt) {
        if (userId == null) {
            throw new IllegalArgumentException("UserId không được null");
        }
        if (provider == null || provider.isBlank()) {
            throw new IllegalArgumentException("Provider không được để trống");
        }
        if (providerUserId == null || providerUserId.isBlank()) {
            throw new IllegalArgumentException("ProviderUserId (sub) không được để trống");
        }
        this.id = id != null ? id : UUID.randomUUID();
        this.userId = userId;
        this.provider = provider.trim().toUpperCase();
        this.providerUserId = providerUserId.trim();
        this.email = email != null ? email.trim().toLowerCase() : "";
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public static ExternalLogin create(UUID userId, String provider, String providerUserId, String email) {
        return new ExternalLogin(UUID.randomUUID(), userId, provider, providerUserId, email, Instant.now());
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getProvider() {
        return provider;
    }

    public String getProviderUserId() {
        return providerUserId;
    }

    public String getEmail() {
        return email;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

