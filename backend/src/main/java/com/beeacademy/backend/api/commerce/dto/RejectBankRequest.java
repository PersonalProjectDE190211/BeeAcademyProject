package com.beeacademy.backend.api.commerce.dto;

import jakarta.validation.constraints.NotBlank;

public record RejectBankRequest(
        @NotBlank(message = "Lý do từ chối không được để trống")
        String reason
) {}

