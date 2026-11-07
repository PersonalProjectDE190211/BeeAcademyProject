package com.beeacademy.backend.api.support.dto;

import jakarta.validation.constraints.NotBlank;

public record AddComplaintMessageRequest(
        @NotBlank(message = "Nội dung phản hồi không được để trống")
        String content
) {}

