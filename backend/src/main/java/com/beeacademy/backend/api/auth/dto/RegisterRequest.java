package com.beeacademy.backend.api.auth.dto;

import com.beeacademy.backend.domain.auth.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Email không được để trống")
        @Email(message = "Định dạng email không hợp lệ")
        String email,

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 6, max = 50, message = "Mật khẩu phải từ 6 đến 50 ký tự")
        String password,

        @NotBlank(message = "Họ và tên không được để trống")
        @Size(max = 150, message = "Họ tên không được vượt quá 150 ký tự")
        String fullName,

        @NotNull(message = "Vai trò người dùng là bắt buộc")
        UserRole role
) {
}

