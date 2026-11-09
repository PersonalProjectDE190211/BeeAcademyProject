package com.beeacademy.backend.api.ai.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;

public record AiChatRequest(
        @NotBlank(message = "Nội dung câu hỏi không được để trống")
        String message,
        String context,
        UUID lessonId,
        UUID courseId,
        List<ChatMessageDto> history
) {
    public record ChatMessageDto(
            String role, // "user" or "model"
            String text
    ) {}
}

