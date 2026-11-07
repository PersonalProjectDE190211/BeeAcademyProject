package com.beeacademy.backend.application.family.dto;

import java.time.Instant;
import java.util.UUID;

public record StudentCourseProgressItem(
        UUID courseId,
        String courseTitle,
        double progressPercent,
        boolean completed,
        Instant lastAccessedAt
) {}

