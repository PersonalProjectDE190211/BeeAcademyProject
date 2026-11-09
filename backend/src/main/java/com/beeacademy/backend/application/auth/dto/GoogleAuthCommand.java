package com.beeacademy.backend.application.auth.dto;

/**
 * Lệnh xác thực bằng Google ID Token từ Frontend.
 *
 * @param idToken Chuỗi ID Token (JWT do Google cấp)
 * @param accountType Loại tài khoản mong muốn khi đăng ký lần đầu: "student" (mặc định) hoặc "parent".
 *                    Tuyệt đối không tin tưởng role teacher/admin từ frontend.
 */
public record GoogleAuthCommand(
        String idToken,
        String accountType
) {
    public GoogleAuthCommand {
        if (idToken != null) {
            idToken = idToken.trim();
        }
        if (accountType != null) {
            accountType = accountType.trim().toLowerCase();
        }
    }
}

