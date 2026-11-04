package com.beeacademy.backend.domain.assessment.event;

import com.beeacademy.backend.domain.common.DomainEvent;
import com.beeacademy.backend.domain.common.ScorePercent;

import java.time.Instant;
import java.util.UUID;

/**
 * Sự kiện: Bài nộp tự luận đã được chấm điểm (bởi Giáo viên hoặc AI).
 */
public record AssignmentGradedEvent(
        UUID eventId,
        UUID submissionId,
        UUID assignmentId,
        UUID studentId,
        UUID gradedById,
        boolean isAiGraded,
        ScorePercent score,
        Instant occurredAt
) implements DomainEvent {

    public static AssignmentGradedEvent of(UUID submissionId, UUID assignmentId, UUID studentId, UUID gradedById, boolean isAi, ScorePercent score) {
        return new AssignmentGradedEvent(UUID.randomUUID(), submissionId, assignmentId, studentId, gradedById, isAi, score, Instant.now());
    }
}
