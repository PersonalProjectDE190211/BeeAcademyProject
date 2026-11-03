package com.beeacademy.backend.domain.course.event;

import com.beeacademy.backend.domain.common.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Sự kiện: Khóa học đã được nộp để chờ kiểm duyệt (UC39).
 */
public record CourseSubmittedForReviewEvent(
        UUID eventId,
        UUID courseId,
        UUID teacherId,
        String courseTitle,
        int totalLessons,
        Instant occurredAt
) implements DomainEvent {

    public static CourseSubmittedForReviewEvent of(UUID courseId, UUID teacherId, String courseTitle, int totalLessons) {
        return new CourseSubmittedForReviewEvent(
                UUID.randomUUID(),
                courseId,
                teacherId,
                courseTitle,
                totalLessons,
                Instant.now()
        );
    }
}
