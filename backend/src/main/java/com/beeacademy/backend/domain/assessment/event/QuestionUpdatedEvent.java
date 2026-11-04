package com.beeacademy.backend.domain.assessment.event;

import com.beeacademy.backend.domain.common.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Sự kiện: Câu hỏi được cập nhật nội dung và tăng version (UC33).
 */
public record QuestionUpdatedEvent(
        UUID eventId,
        UUID questionId,
        UUID teacherId,
        int newVersionNo,
        Instant occurredAt
) implements DomainEvent {

    public static QuestionUpdatedEvent of(UUID questionId, UUID teacherId, int newVersionNo) {
        return new QuestionUpdatedEvent(UUID.randomUUID(), questionId, teacherId, newVersionNo, Instant.now());
    }
}
