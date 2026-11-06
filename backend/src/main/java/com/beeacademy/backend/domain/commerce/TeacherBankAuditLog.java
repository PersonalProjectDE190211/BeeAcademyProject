package com.beeacademy.backend.domain.commerce;

import com.beeacademy.backend.domain.common.DomainException;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity ghi lại vết kiểm toán (Audit Log) khi giáo viên hoặc admin thay đổi số tài khoản ngân hàng.
 */
public class TeacherBankAuditLog implements Serializable {

    private final UUID id;
    private final UUID teacherId;
    private final Instant changedAt;
    private final String changedByName;
    private final String reason;
    private final String changesJson;

    public TeacherBankAuditLog(
            UUID id,
            UUID teacherId,
            Instant changedAt,
            String changedByName,
            String reason,
            String changesJson
    ) {
        if (id == null) {
            throw new DomainException("INVALID_AUDIT_LOG", "ID nhật ký không được null");
        }
        if (teacherId == null) {
            throw new DomainException("INVALID_AUDIT_LOG", "Teacher ID không được null");
        }
        this.id = id;
        this.teacherId = teacherId;
        this.changedAt = changedAt != null ? changedAt : Instant.now();
        this.changedByName = changedByName == null ? "SYSTEM" : changedByName;
        this.reason = reason;
        this.changesJson = changesJson == null ? "{}" : changesJson;
    }

    public static TeacherBankAuditLog create(UUID teacherId, String changedByName, String reason, String changesJson) {
        return new TeacherBankAuditLog(UUID.randomUUID(), teacherId, Instant.now(), changedByName, reason, changesJson);
    }

    public UUID getId() {
        return id;
    }

    public UUID getTeacherId() {
        return teacherId;
    }

    public Instant getChangedAt() {
        return changedAt;
    }

    public String getChangedByName() {
        return changedByName;
    }

    public String getReason() {
        return reason;
    }

    public String getChangesJson() {
        return changesJson;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeacherBankAuditLog that = (TeacherBankAuditLog) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

