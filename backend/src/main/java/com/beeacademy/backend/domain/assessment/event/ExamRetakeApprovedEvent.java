package com.beeacademy.backend.domain.assessment.event;

import com.beeacademy.backend.domain.common.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Sự kiện: Giáo viên phê duyệt đơn xin thi lại của học sinh.
 */
public record ExamRetakeApprovedEvent(
        UUID eventId,
        UUID requestId,
        UUID examConfigId,
        UUID studentId,
        UUID approvedByTeacherId,
        Instant grantExpiresAt,
        Instant occurredAt
) implements DomainEvent {

    public static ExamRetakeApprovedEvent of(UUID requestId, UUID examConfigId, UUID studentId, UUID teacherId, Instant grantExpiresAt) {
        return new ExamRetakeApprovedEvent(UUID.randomUUID(), requestId, examConfigId, studentId, teacherId, grantExpiresAt, Instant.now());
    }
}
