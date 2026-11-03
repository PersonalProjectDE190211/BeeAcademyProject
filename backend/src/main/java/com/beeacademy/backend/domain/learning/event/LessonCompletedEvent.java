package com.beeacademy.backend.domain.learning.event;

import com.beeacademy.backend.domain.common.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Sự kiện: Học viên đã hoàn thành một bài học (xem >= 80% thời lượng theo UC14).
 */
public record LessonCompletedEvent(
        UUID eventId,
        UUID studentId,
        UUID courseId,
        UUID lessonId,
        Instant occurredAt
) implements DomainEvent {

    public static LessonCompletedEvent of(UUID studentId, UUID courseId, UUID lessonId) {
        return new LessonCompletedEvent(
                UUID.randomUUID(),
                studentId,
                courseId,
                lessonId,
                Instant.now()
        );
    }
}
