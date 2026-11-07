package com.beeacademy.backend.infrastructure.commerce.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "teacher_bank_audit_log")
public class TeacherBankAuditLogJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "teacher_id", nullable = false)
    private UUID teacherId;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    @Column(name = "changed_by_name", length = 200)
    private String changedByName;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "changes", columnDefinition = "TEXT")
    private String changes;

    public TeacherBankAuditLogJpaEntity() {
    }

    public TeacherBankAuditLogJpaEntity(
            UUID id,
            UUID teacherId,
            Instant changedAt,
            String changedByName,
            String reason,
            String changes
    ) {
        this.id = id;
        this.teacherId = teacherId;
        this.changedAt = changedAt;
        this.changedByName = changedByName;
        this.reason = reason;
        this.changes = changes;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(UUID teacherId) {
        this.teacherId = teacherId;
    }

    public Instant getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(Instant changedAt) {
        this.changedAt = changedAt;
    }

    public String getChangedByName() {
        return changedByName;
    }

    public void setChangedByName(String changedByName) {
        this.changedByName = changedByName;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getChanges() {
        return changes;
    }

    public void setChanges(String changes) {
        this.changes = changes;
    }
}

