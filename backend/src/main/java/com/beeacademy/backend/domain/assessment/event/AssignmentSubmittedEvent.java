package com.beeacademy.backend.domain.assessment.event;

import com.beeacademy.backend.domain.common.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Sự kiện: Học sinh nộp bài tập tự luận (Assignment).
 */
public record AssignmentSubmittedEvent(
        UUID eventId,
        UUID submissionId,
        UUID assignmentId,
        UUID studentId,
        Instant occurredAt
) implements DomainEvent {

    public static AssignmentSubmittedEvent of(UUID submissionId, UUID assignmentId, UUID studentId) {
        return new AssignmentSubmittedEvent(UUID.randomUUID(), submissionId, assignmentId, studentId, Instant.now());
    }
}
