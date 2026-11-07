package com.beeacademy.backend.api.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record RejectTeacherRequest(
        @NotBlank(message = "Lý do từ chối không được để trống")
        String reason
) {
}

