package com.beeacademy.backend.api.family.dto;

import jakarta.validation.constraints.NotBlank;

public record RespondLinkRequest(
        @NotBlank(message = "Mã lời mời không được để trống")
        String inviteToken,

        String reason
) {}

