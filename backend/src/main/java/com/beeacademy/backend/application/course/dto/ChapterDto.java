package com.beeacademy.backend.application.course.dto;

import com.beeacademy.backend.domain.course.Chapter;

import java.util.List;
import java.util.UUID;

public record ChapterDto(
        UUID id,
        String title,
        String description,
        int orderIndex,
        List<LessonDto> lessons
) {
    public static ChapterDto from(Chapter chapter, boolean includeFullVideoUrl) {
        List<LessonDto> lessonDtos = chapter.getLessons() != null
                ? chapter.getLessons().stream()
                        .map(l -> LessonDto.from(l, includeFullVideoUrl))
                        .toList()
                : List.of();

        return new ChapterDto(
                chapter.getId(),
                chapter.getTitle(),
                chapter.getDescription(),
                chapter.getOrderIndex(),
                lessonDtos
        );
    }
}
