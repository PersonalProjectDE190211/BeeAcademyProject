package com.beeacademy.backend.api.course.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record SubmitReviewRequest(
        @Min(value = 1, message = "Đánh giá tối thiểu là 1 sao")
        @Max(value = 5, message = "Đánh giá tối đa là 5 sao")
        int rating,

        @Size(max = 1000, message = "Bình luận nhận xét không được vượt quá 1000 ký tự")
        String comment
) {
}

