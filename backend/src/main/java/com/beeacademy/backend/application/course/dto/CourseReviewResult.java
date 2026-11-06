package com.beeacademy.backend.application.course.dto;

import com.beeacademy.backend.domain.course.CourseReview;

import java.time.Instant;
import java.util.UUID;

public record CourseReviewResult(
        UUID id,
        UUID courseId,
        UUID studentId,
        int rating,
        String comment,
        Instant createdAt
) {
    public static CourseReviewResult from(CourseReview review) {
        return new CourseReviewResult(
                review.getId(),
                review.getCourseId(),
                review.getStudentId(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }
}
