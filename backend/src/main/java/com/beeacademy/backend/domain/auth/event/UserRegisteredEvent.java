package com.beeacademy.backend.domain.auth.event;

import com.beeacademy.backend.domain.auth.UserRole;
import com.beeacademy.backend.domain.common.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Sự kiện phát sinh khi một người dùng mới được tạo thành công trong hệ thống.
 */
public record UserRegisteredEvent(
        UUID eventId,
        UUID userId,
        String email,
        UserRole role,
        Instant occurredAt
) implements DomainEvent {

    public static UserRegisteredEvent of(UUID userId, String email, UserRole role) {
        return new UserRegisteredEvent(UUID.randomUUID(), userId, email, role, Instant.now());
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

