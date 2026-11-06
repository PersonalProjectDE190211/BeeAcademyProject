package com.beeacademy.backend.application.course.dto;

import com.beeacademy.backend.domain.course.Lesson;

import java.util.UUID;

public record LessonDto(
        UUID id,
        String title,
        int orderIndex,
        String videoUrl,
        int durationSec,
        boolean isFreePreview
) {
    public static LessonDto from(Lesson lesson, boolean includeFullVideoUrl) {
        return new LessonDto(
                lesson.getId(),
                lesson.getTitle(),
                lesson.getOrderIndex(),
                (lesson.isFreePreview() || includeFullVideoUrl) ? lesson.getVideoUrl() : null,
                lesson.getDurationSec(),
                lesson.isFreePreview()
        );
    }
}

