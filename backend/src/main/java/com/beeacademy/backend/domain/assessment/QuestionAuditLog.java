package com.beeacademy.backend.domain.assessment;

import com.beeacademy.backend.domain.common.DomainException;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity ghi nhận nhật ký kiểm toán (Audit Log) mọi hành vi tác động lên câu hỏi (UC33).
 */
public class QuestionAuditLog {

    private final UUID id;
    private final UUID questionId;
    private final UUID teacherId;
    private final String action;
    private final String details;
    private final Instant occurredAt;

    public QuestionAuditLog(UUID id, UUID questionId, UUID teacherId, String action, String details, Instant occurredAt) {
        if (id == null) throw new DomainException("INVALID_ID", "ID không được null");
        if (questionId == null) throw new DomainException("INVALID_QUESTION_ID", "Question ID không được null");
        if (action == null || action.isBlank()) throw new DomainException("INVALID_ACTION", "Action không được để trống");

        this.id = id;
        this.questionId = questionId;
        this.teacherId = teacherId;
        this.action = action.trim();
        this.details = details != null ? details.trim() : "";
        this.occurredAt = occurredAt != null ? occurredAt : Instant.now();
    }

    public static QuestionAuditLog log(UUID questionId, UUID teacherId, String action, String details) {
        return new QuestionAuditLog(UUID.randomUUID(), questionId, teacherId, action, details, Instant.now());
    }

    public UUID getId() { return id; }
    public UUID getQuestionId() { return questionId; }
    public UUID getTeacherId() { return teacherId; }
    public String getAction() { return action; }
    public String getDetails() { return details; }
    public Instant getOccurredAt() { return occurredAt; }
}
