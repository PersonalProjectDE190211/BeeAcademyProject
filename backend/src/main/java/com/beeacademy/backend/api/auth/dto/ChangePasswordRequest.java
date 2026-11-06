package com.beeacademy.backend.api.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "Mật khẩu cũ không được để trống")
        String oldPassword,

        @NotBlank(message = "Mật khẩu mới không được để trống")
        @Size(min = 6, max = 50, message = "Mật khẩu mới phải từ 6 đến 50 ký tự")
        String newPassword
) {
}

