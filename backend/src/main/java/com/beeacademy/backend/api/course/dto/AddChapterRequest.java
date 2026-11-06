package com.beeacademy.backend.api.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddChapterRequest(
        @NotBlank(message = "Tiêu đề chương không được để trống")
        @Size(max = 250, message = "Tiêu đề chương không được vượt quá 250 ký tự")
        String title,

        String description
) {
}

