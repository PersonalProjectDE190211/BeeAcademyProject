package com.beeacademy.backend.domain.assessment.event;

import com.beeacademy.backend.domain.common.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Sự kiện: Học sinh gửi đơn xin mở lại lượt thi kiểm tra (UC17-19).
 */
public record ExamRetakeRequestedEvent(
        UUID eventId,
        UUID requestId,
        UUID examConfigId,
        UUID studentId,
        int requestCount,
        Instant occurredAt
) implements DomainEvent {

    public static ExamRetakeRequestedEvent of(UUID requestId, UUID examConfigId, UUID studentId, int requestCount) {
        return new ExamRetakeRequestedEvent(UUID.randomUUID(), requestId, examConfigId, studentId, requestCount, Instant.now());
    }
}
