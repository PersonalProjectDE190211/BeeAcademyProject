package com.beeacademy.backend.application.course.dto;

import com.beeacademy.backend.domain.course.Course;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CourseDetailResult(
        UUID id,
        UUID teacherId,
        UUID categoryId,
        String title,
        String slug,
        String description,
        String thumbnailUrl,
        int priceVnd,
        String status,
        List<Integer> grades,
        int totalChapters,
        int totalLessons,
        int totalDurationSec,
        BigDecimal averageRating,
        int totalReviews,
        Instant publishedAt,
        Instant createdAt,
        Instant updatedAt,
        List<ChapterDto> chapters
) {
    public static CourseDetailResult from(Course course, boolean includeFullVideoUrls) {
        List<ChapterDto> chapterDtos = course.getChapters() != null
                ? course.getChapters().stream()
                        .map(ch -> ChapterDto.from(ch, includeFullVideoUrls))
                        .toList()
                : List.of();

        return new CourseDetailResult(
                course.getId(),
                course.getTeacherId(),
                course.getCategoryId(),
                course.getTitle(),
                course.getSlug().value(),
                course.getDescription(),
                course.getThumbnailUrl(),
                course.getPrice().amount(),
                course.getStatus().name(),
                course.getGrades(),
                course.getTotalChapters(),
                course.getTotalLessons(),
                course.getTotalDurationSec(),
                course.getAverageRating(),
                course.getTotalReviews(),
                course.getPublishedAt(),
                course.getCreatedAt(),
                course.getUpdatedAt(),
                chapterDtos
        );
    }
}

