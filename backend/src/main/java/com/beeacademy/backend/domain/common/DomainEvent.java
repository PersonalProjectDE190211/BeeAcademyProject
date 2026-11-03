package com.beeacademy.backend.domain.common;

import java.time.Instant;
import java.util.UUID;

/**
 * Giao diện cơ sở cho tất cả Sự kiện nghiệp vụ (Domain Event) trong hệ thống.
 * Thiết kế tương thích hoàn hảo cả Record style (eventId(), occurredAt())
 * và JavaBean getter style (getEventId(), getOccurredAt()).
 */
public interface DomainEvent {

    /**
     * Mã định danh duy nhất của sự kiện (dùng cho deduplication / idempotency tracking).
     */
    UUID eventId();

    /**
     * Thời điểm sự kiện phát sinh.
     */
    Instant occurredAt();

    default UUID getEventId() {
        return eventId();
    }

    default Instant getOccurredAt() {
        return occurredAt();
    }
}
