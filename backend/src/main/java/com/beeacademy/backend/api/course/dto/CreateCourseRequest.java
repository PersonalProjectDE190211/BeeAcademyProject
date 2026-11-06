package com.beeacademy.backend.api.course.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CreateCourseRequest(
        @NotNull(message = "Danh mục môn học không được để trống")
        UUID categoryId,

        @NotBlank(message = "Tiêu đề khóa học không được để trống")
        @Size(max = 250, message = "Tiêu đề khóa học không được vượt quá 250 ký tự")
        String title,

        String description,
        String thumbnailUrl,

        @Min(value = 0, message = "Học phí khóa học không được âm")
        int priceVnd,

        List<Integer> grades
) {
}
