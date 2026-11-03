package com.beeacademy.backend.domain.learning.event;

import com.beeacademy.backend.domain.common.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Sự kiện: Chứng chỉ hoàn thành khóa học đã được cấp phát cho học viên (UC20).
 */
public record CertificateIssuedEvent(
        UUID eventId,
        UUID certificateId,
        UUID studentId,
        UUID courseId,
        String certificateCode,
        Instant occurredAt
) implements DomainEvent {

    public static CertificateIssuedEvent of(UUID certificateId, UUID studentId, UUID courseId, String certificateCode) {
        return new CertificateIssuedEvent(
                UUID.randomUUID(),
                certificateId,
                studentId,
                courseId,
                certificateCode,
                Instant.now()
        );
    }
}

