package com.beeacademy.backend.api.ai.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record ChatbotMessageRequest(
        UUID conversationId,
        @NotBlank(message = "Nội dung câu hỏi không được để trống")
        String message
) {
}

