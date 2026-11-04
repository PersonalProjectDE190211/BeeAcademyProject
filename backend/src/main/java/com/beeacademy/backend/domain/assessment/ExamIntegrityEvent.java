package com.beeacademy.backend.domain.assessment;

import com.beeacademy.backend.domain.common.DomainException;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity ghi nhận một hành vi vi phạm liêm chính trong lúc làm bài thi (UC44).
 * Ví dụ: chuyển tab, thoát chế độ toàn màn hình, paste dữ liệu từ ngoài vào.
 */
public class ExamIntegrityEvent {

    private final UUID id;
    private final UUID examAttemptId;
    private final IntegrityEventType eventType;
    private final String details;
    private final Instant occurredAt;

    public ExamIntegrityEvent(UUID id, UUID examAttemptId, IntegrityEventType eventType, String details, Instant occurredAt) {
        if (id == null) throw new DomainException("INVALID_ID", "ID không được null");
        if (examAttemptId == null) throw new DomainException("INVALID_ATTEMPT_ID", "ExamAttempt ID không được null");
        if (eventType == null) throw new DomainException("INVALID_EVENT_TYPE", "Loại vi phạm không được null");

        this.id = id;
        this.examAttemptId = examAttemptId;
        this.eventType = eventType;
        this.details = details != null ? details.trim() : "";
        this.occurredAt = occurredAt != null ? occurredAt : Instant.now();
    }

    public static ExamIntegrityEvent record(UUID examAttemptId, IntegrityEventType eventType, String details) {
        return new ExamIntegrityEvent(UUID.randomUUID(), examAttemptId, eventType, details, Instant.now());
    }

    public UUID getId() { return id; }
    public UUID getExamAttemptId() { return examAttemptId; }
    public IntegrityEventType getEventType() { return eventType; }
    public String getDetails() { return details; }
    public Instant getOccurredAt() { return occurredAt; }
}
