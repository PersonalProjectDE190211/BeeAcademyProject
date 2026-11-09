package com.beeacademy.backend.application.learning.dto;

import com.beeacademy.backend.application.course.dto.CourseDocumentDto;

import java.util.List;
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
        double watchPercent,
        List<CourseDocumentDto> documents
) {
    public LearningLessonResult(
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
        this(lessonId, chapterId, title, orderIndex, videoUrl, durationSec, isFreePreview, isCompleted, lastPositionSec, watchPercent, List.of());
    }
}
