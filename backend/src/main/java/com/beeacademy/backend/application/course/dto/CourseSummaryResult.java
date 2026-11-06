package com.beeacademy.backend.application.course.dto;

import com.beeacademy.backend.domain.course.Course;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CourseSummaryResult(
        UUID id,
        UUID teacherId,
        UUID categoryId,
        String title,
        String slug,
        String description,
        String thumbnailUrl,
        int priceVnd,
        String status,
        int totalChapters,
        int totalLessons,
        int totalDurationSec,
        BigDecimal averageRating,
        int totalReviews,
        Instant publishedAt,
        Instant createdAt
) {
    public static CourseSummaryResult from(Course course) {
        return new CourseSummaryResult(
                course.getId(),
                course.getTeacherId(),
                course.getCategoryId(),
                course.getTitle(),
                course.getSlug().value(),
                course.getDescription(),
                course.getThumbnailUrl(),
                course.getPrice().amount(),
                course.getStatus().name(),
                course.getTotalChapters(),
                course.getTotalLessons(),
                course.getTotalDurationSec(),
                course.getAverageRating(),
                course.getTotalReviews(),
                course.getPublishedAt(),
                course.getCreatedAt()
        );
    }
}
