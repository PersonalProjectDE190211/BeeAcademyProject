package com.beeacademy.backend.domain.auth;

import com.beeacademy.backend.domain.common.DomainException;

import java.io.Serializable;

/**
 * Value Object biểu diễn mật khẩu đã được băm an toàn (BCrypt).
 * Đảm bảo mật khẩu lưu trong Domain luôn là bản băm (Hashed), tuyệt đối không bao giờ chứa mật khẩu dạng rõ (Plaintext).
 */
public record PasswordHash(String value) implements Serializable {

    public PasswordHash {
        if (value == null || value.isBlank()) {
            throw new DomainException("INVALID_PASSWORD_HASH", "Mật khẩu băm không được để trống");
        }
    }

    public static PasswordHash of(String hash) {
        return new PasswordHash(hash);
    }

    @Override
    public String toString() {
        return "********"; // Che giấu khi in log
    }
}

