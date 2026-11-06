package com.beeacademy.backend.application.course.dto;

import java.util.UUID;

public record SubmitReviewCommand(
        UUID courseId,
        UUID studentId,
        int rating,
        String comment
) {
}
