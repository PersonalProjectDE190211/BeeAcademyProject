package com.beeacademy.backend.api.learning.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AskQuestionRequest(
        @NotBlank(message = "Tiêu đề câu hỏi không được để trống")
        @Size(max = 250, message = "Tiêu đề không được vượt quá 250 ký tự")
        String title,

        @NotBlank(message = "Nội dung câu hỏi không được để trống")
        String content
) {
}
