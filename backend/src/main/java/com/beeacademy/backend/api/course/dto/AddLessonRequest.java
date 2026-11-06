package com.beeacademy.backend.api.course.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddLessonRequest(
        @NotBlank(message = "Tiêu đề bài học không được để trống")
        @Size(max = 250, message = "Tiêu đề bài học không được vượt quá 250 ký tự")
        String title,

        String videoUrl,

        @Min(value = 0, message = "Thời lượng bài học không được âm")
        int durationSec,

        boolean isFreePreview
) {
}

