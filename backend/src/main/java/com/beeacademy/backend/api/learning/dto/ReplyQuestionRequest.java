package com.beeacademy.backend.api.learning.dto;

import jakarta.validation.constraints.NotBlank;

public record ReplyQuestionRequest(
        @NotBlank(message = "Nội dung phản hồi không được để trống")
        String content
) {
}
