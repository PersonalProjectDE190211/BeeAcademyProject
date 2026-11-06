package com.beeacademy.backend.api.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank(message = "Họ và tên không được để trống")
        @Size(max = 150, message = "Họ tên không được vượt quá 150 ký tự")
        String fullName,

        @Size(max = 30, message = "Số điện thoại không được vượt quá 30 ký tự")
        String phone,

        @Size(max = 1000, message = "Tiểu sử không được vượt quá 1000 ký tự")
        String bio
) {
}

