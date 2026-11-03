package com.beeacademy.backend.domain.auth.event;

import com.beeacademy.backend.domain.common.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Sự kiện phát sinh khi tài khoản người dùng bị khóa tạm do 5 lần đăng nhập sai.
 */
public record AccountLockedEvent(
        UUID eventId,
        UUID userId,
        Instant lockedUntil,
        Instant occurredAt
) implements DomainEvent {

    public static AccountLockedEvent of(UUID userId, Instant lockedUntil) {
        return new AccountLockedEvent(UUID.randomUUID(), userId, lockedUntil, Instant.now());
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

