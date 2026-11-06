package com.beeacademy.backend.application.learning.dto;

import java.time.Instant;
import java.util.UUID;

public record LessonProgressResult(
        UUID lessonId,
        UUID courseId,
        int watchedSeconds,
        int lastPositionSec,
        double watchPercent,
        boolean isCompleted,
        Instant completedAt,
        double courseProgressPercent,
        boolean courseCompleted
) {
}
