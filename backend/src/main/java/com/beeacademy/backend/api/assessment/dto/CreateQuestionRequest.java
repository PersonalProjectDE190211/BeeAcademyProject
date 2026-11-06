package com.beeacademy.backend.api.assessment.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateQuestionRequest(
        UUID categoryId,
        UUID chapterId,
        int grade,
        @NotBlank(message = "Nội dung câu hỏi không được để trống")
        String content,
        String explanation,
        @NotNull(message = "Độ khó không được để trống")
        String difficulty,
        @NotNull(message = "Loại câu hỏi không được để trống")
        String type,
        @NotEmpty(message = "Danh sách phương án lựa chọn không được rỗng")
        @Valid
        List<ChoiceItemRequest> choices,
        List<String> tags
) {
}
