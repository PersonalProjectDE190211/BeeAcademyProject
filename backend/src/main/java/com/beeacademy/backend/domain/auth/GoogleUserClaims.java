package com.beeacademy.backend.domain.auth;

/**
 * Domain Record biểu diễn các claims đã được xác thực bảo mật từ Google ID Token:
 * - sub: Định danh duy nhất và bất biến của tài khoản Google.
 * - email: Địa chỉ email của người dùng.
 * - emailVerified: Trạng thái email đã được Google xác thực (bắt buộc true).
 * - fullName: Họ và tên hiển thị.
 * - avatarUrl: Đường dẫn ảnh đại diện.
 * - issuer: Bên phát hành token (accounts.google.com hoặc https://accounts.google.com).
 * - audience: Khớp với Google Client ID của ứng dụng.
 */
public record GoogleUserClaims(
        String sub,
        String email,
        boolean emailVerified,
        String fullName,
        String avatarUrl,
        String issuer,
        String audience
) {
    public GoogleUserClaims {
        if (sub == null || sub.isBlank()) {
            throw new IllegalArgumentException("Google subject (sub) không được rỗng");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Google email không được rỗng");
        }
    }
}

