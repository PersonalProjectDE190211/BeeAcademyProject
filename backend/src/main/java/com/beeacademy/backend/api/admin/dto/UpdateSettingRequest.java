package com.beeacademy.backend.api.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateSettingRequest(
        @NotBlank(message = "Giá trị cấu hình không được để trống")
        String value,
        String reason
) {
}

