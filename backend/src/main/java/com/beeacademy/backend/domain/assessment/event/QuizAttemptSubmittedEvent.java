package com.beeacademy.backend.domain.assessment.event;

import com.beeacademy.backend.domain.common.DomainEvent;
import com.beeacademy.backend.domain.common.ScorePercent;

import java.time.Instant;
import java.util.UUID;

/**
 * Sự kiện: Học sinh nộp bài quiz chương và đã được chấm điểm.
 */
public record QuizAttemptSubmittedEvent(
        UUID eventId,
        UUID attemptId,
        UUID studentId,
        UUID chapterId,
        ScorePercent score,
        boolean passed,
        Instant occurredAt
) implements DomainEvent {

    public static QuizAttemptSubmittedEvent of(UUID attemptId, UUID studentId, UUID chapterId, ScorePercent score, boolean passed) {
        return new QuizAttemptSubmittedEvent(UUID.randomUUID(), attemptId, studentId, chapterId, score, passed, Instant.now());
    }
}
