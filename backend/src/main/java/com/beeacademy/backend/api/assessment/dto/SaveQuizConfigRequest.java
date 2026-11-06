package com.beeacademy.backend.api.assessment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SaveQuizConfigRequest(
        @NotNull(message = "Chapter ID không được null")
        UUID chapterId,
        @Min(value = 1, message = "Tổng số câu hỏi phải >= 1")
        int totalQuestions,
        @Min(value = 0, message = "Số câu dễ không được âm")
        int easyCount,
        @Min(value = 0, message = "Số câu trung bình không được âm")
        int mediumCount,
        @Min(value = 0, message = "Số câu khó không được âm")
        int hardCount,
        Integer timeLimitMinutes,
        @DecimalMin(value = "0.0", message = "Điểm qua môn phải từ 0.0 đến 100.0")
        double passingScore,
        boolean shuffleQuestions,
        boolean shuffleChoices,
        Integer maxAttempts
) {
}
