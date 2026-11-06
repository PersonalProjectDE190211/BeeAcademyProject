package com.beeacademy.backend.application.learning.dto;

import java.time.Instant;
import java.util.UUID;

public record EnrollmentSummaryResult(
        UUID id,
        UUID courseId,
        String courseTitle,
        String courseSlug,
        String courseThumbnailUrl,
        int completedLessons,
        int totalLessons,
        double progressPercent,
        boolean completed,
        Instant enrolledAt,
        Instant completedAt
) {
}
