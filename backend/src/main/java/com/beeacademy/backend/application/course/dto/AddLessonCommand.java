package com.beeacademy.backend.application.course.dto;

import java.util.UUID;

public record AddLessonCommand(
        UUID courseId,
        UUID teacherId,
        UUID chapterId,
        String title,
        String videoUrl,
        int durationSec,
        boolean isFreePreview
) {
}

