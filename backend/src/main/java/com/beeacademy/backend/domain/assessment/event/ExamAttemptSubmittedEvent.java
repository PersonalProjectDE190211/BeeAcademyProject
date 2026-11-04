package com.beeacademy.backend.domain.assessment.event;

import com.beeacademy.backend.domain.common.DomainEvent;
import com.beeacademy.backend.domain.common.ScorePercent;

import java.time.Instant;
import java.util.UUID;

/**
 * Sự kiện: Học sinh nộp bài thi chính thức (Exam).
 */
public record ExamAttemptSubmittedEvent(
        UUID eventId,
        UUID attemptId,
        UUID examConfigId,
        UUID studentId,
        ScorePercent score,
        boolean passed,
        Instant occurredAt
) implements DomainEvent {

    public static ExamAttemptSubmittedEvent of(UUID attemptId, UUID examConfigId, UUID studentId, ScorePercent score, boolean passed) {
        return new ExamAttemptSubmittedEvent(UUID.randomUUID(), attemptId, examConfigId, studentId, score, passed, Instant.now());
    }
}
