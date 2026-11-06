package com.beeacademy.backend.application.learning.dto;

import java.util.UUID;

public record LearningLessonResult(
        UUID lessonId,
        UUID chapterId,
        String title,
        int orderIndex,
        String videoUrl,
        int durationSec,
        boolean isFreePreview,
        boolean isCompleted,
        int lastPositionSec,
        double watchPercent
) {
}
