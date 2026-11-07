package com.beeacademy.backend.api.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record LockUserRequest(
        @NotBlank(message = "Lý do khóa tài khoản không được để trống")
        String reason
) {
}

