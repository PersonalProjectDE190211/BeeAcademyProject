package com.beeacademy.backend.domain.assessment.event;

import com.beeacademy.backend.domain.common.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Sự kiện: Câu hỏi mới được thêm vào ngân hàng câu hỏi.
 */
public record QuestionCreatedEvent(
        UUID eventId,
        UUID questionId,
        UUID teacherId,
        Instant occurredAt
) implements DomainEvent {

    public static QuestionCreatedEvent of(UUID questionId, UUID teacherId) {
        return new QuestionCreatedEvent(UUID.randomUUID(), questionId, teacherId, Instant.now());
    }
}
