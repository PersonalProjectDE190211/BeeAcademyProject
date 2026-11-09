package com.beeacademy.backend.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Yêu cầu xác thực đăng nhập/đăng ký bằng Google OAuth2 ID Token")
public record GoogleAuthRequest(
        @Schema(description = "ID Token (JWT) do Google cấp từ phía client", requiredMode = Schema.RequiredMode.REQUIRED, example = "eyJhbGciOiJSUzI1NiIs...")
        @NotBlank(message = "Google ID Token không được để trống")
        String idToken,

        @Schema(description = "Loại tài khoản mong muốn khi tạo mới: 'student' (mặc định) hoặc 'parent'", example = "student")
        String accountType
) {}

