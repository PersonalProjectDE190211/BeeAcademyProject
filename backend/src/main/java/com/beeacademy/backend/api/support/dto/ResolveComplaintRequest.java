package com.beeacademy.backend.api.support.dto;

import jakarta.validation.constraints.NotBlank;

public record ResolveComplaintRequest(
        @NotBlank(message = "Tóm tắt hướng giải quyết không được để trống")
        String resolutionSummary
) {}

