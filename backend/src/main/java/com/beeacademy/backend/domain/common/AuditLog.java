package com.beeacademy.backend.domain.common;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Thực thể Nhật ký kiểm toán tập trung bất biến (Append-Only Audit Log).
 * Thay thế toàn bộ các bảng audit rời rạc:
 * - Duyệt khóa học (COURSE_APPROVAL)
 * - Đổi tài khoản ngân hàng giáo viên (TEACHER_BANK_ACCOUNT)
 * - Chấm/sửa điểm bài thi (ASSESSMENT_ATTEMPT_GRADE)
 * - Liên kết & truy cập tiến độ phụ huynh (PARENT_STUDENT_LINK, PARENT_PROGRESS_ACCESS)
 */
public class AuditLog implements Serializable {

    private final UUID id;
    private final AuditEntityType entityType;
    private final UUID entityId;
    private final String action;
    private final UUID actorId;
    private final String actorRole;
    private final String reason;
    private final String diffJson;
    private final String metadataJson;
    private final Instant createdAt;

    public AuditLog(
            UUID id,
            AuditEntityType entityType,
            UUID entityId,
            String action,
            UUID actorId,
            String actorRole,
            String reason,
            String diffJson,
            String metadataJson,
            Instant createdAt
    ) {
        if (id == null) {
            throw new DomainException("INVALID_AUDIT_LOG", "Audit Log ID không được null");
        }
        if (entityType == null) {
            throw new DomainException("INVALID_AUDIT_LOG", "Loại thực thể kiểm toán (entityType) không được null");
        }
        if (entityId == null) {
            throw new DomainException("INVALID_AUDIT_LOG", "Entity ID không được null");
        }
        if (action == null || action.isBlank()) {
            throw new DomainException("INVALID_AUDIT_LOG", "Hành động kiểm toán (action) không được để trống");
        }
        this.id = id;
        this.entityType = entityType;
        this.entityId = entityId;
        this.action = action.trim().toUpperCase();
        this.actorId = actorId;
        this.actorRole = actorRole == null ? "SYSTEM" : actorRole.trim().toUpperCase();
        this.reason = reason;
        this.diffJson = diffJson == null || diffJson.isBlank() ? "{}" : diffJson;
        this.metadataJson = metadataJson == null || metadataJson.isBlank() ? "{}" : metadataJson;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public static AuditLog record(
            AuditEntityType entityType,
            UUID entityId,
            String action,
            UUID actorId,
            String actorRole,
            String reason,
            String diffJson
    ) {
        return new AuditLog(
                UUID.randomUUID(),
                entityType,
                entityId,
                action,
                actorId,
                actorRole,
                reason,
                diffJson,
                "{}",
                Instant.now()
        );
    }

    public UUID getId() {
        return id;
    }

    public AuditEntityType getEntityType() {
        return entityType;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public String getAction() {
        return action;
    }

    public UUID getActorId() {
        return actorId;
    }

    public String getActorRole() {
        return actorRole;
    }

    public String getReason() {
        return reason;
    }

    public String getDiffJson() {
        return diffJson;
    }

    public String getMetadataJson() {
        return metadataJson;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditLog auditLog = (AuditLog) o;
        return Objects.equals(id, auditLog.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
