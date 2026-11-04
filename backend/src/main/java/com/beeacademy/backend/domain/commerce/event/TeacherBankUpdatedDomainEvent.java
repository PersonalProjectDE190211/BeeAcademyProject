package com.beeacademy.backend.domain.commerce.event;

import com.beeacademy.backend.domain.common.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record TeacherBankUpdatedDomainEvent(
        UUID eventId,
        UUID bankAccountId,
        UUID teacherId,
        String bankName,
        String accountNumber,
        String accountHolder,
        Instant occurredAt
) implements DomainEvent {
    public TeacherBankUpdatedDomainEvent(
            UUID bankAccountId,
            UUID teacherId,
            String bankName,
            String accountNumber,
            String accountHolder
    ) {
        this(UUID.randomUUID(), bankAccountId, teacherId, bankName, accountNumber, accountHolder, Instant.now());
    }
}
