package com.beeacademy.backend.api.assessment.dto;

import jakarta.validation.constraints.NotBlank;

public record ChoiceItemRequest(
        @NotBlank(message = "Nội dung phương án không được để trống")
        String content,
        String imageUrl,
        boolean isCorrect,
        int position
) {
}
