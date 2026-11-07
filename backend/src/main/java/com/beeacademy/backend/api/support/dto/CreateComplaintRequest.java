package com.beeacademy.backend.api.support.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateComplaintRequest(
        @NotBlank(message = "Tiêu đề khiếu nại không được để trống")
        String title,

        String category,

        String priority,

        @NotBlank(message = "Nội dung phản ánh không được để trống")
        String content
) {}

