package com.beeacademy.backend.domain.learning.event;

import com.beeacademy.backend.domain.common.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Sự kiện: Học viên đã hoàn thành 100% các bài học trong khóa học.
 */
public record CourseCompletedEvent(
        UUID eventId,
        UUID studentId,
        UUID courseId,
        Instant occurredAt
) implements DomainEvent {

    public static CourseCompletedEvent of(UUID studentId, UUID courseId) {
        return new CourseCompletedEvent(
                UUID.randomUUID(),
                studentId,
                courseId,
                Instant.now()
        );
    }
}

