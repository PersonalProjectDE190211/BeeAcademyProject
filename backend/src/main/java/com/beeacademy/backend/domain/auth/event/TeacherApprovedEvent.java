package com.beeacademy.backend.domain.auth.event;

import com.beeacademy.backend.domain.common.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Sự kiện phát sinh khi tài khoản Giáo viên được phê duyệt.
 */
public record TeacherApprovedEvent(
        UUID eventId,
        UUID teacherId,
        UUID approvedByAdminId,
        Instant occurredAt
) implements DomainEvent {

    public static TeacherApprovedEvent of(UUID teacherId, UUID approvedByAdminId) {
        return new TeacherApprovedEvent(UUID.randomUUID(), teacherId, approvedByAdminId, Instant.now());
    }

    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }
}

