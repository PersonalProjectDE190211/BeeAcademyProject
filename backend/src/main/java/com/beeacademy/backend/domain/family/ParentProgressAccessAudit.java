package com.beeacademy.backend.domain.family;

import com.beeacademy.backend.domain.common.DomainException;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity ghi vết truy cập dữ liệu học tập nhạy cảm của Học sinh từ Phụ huynh (UC24).
 * Đảm bảo tính tuân thủ quyền riêng tư dữ liệu cá nhân.
 */
public class ParentProgressAccessAudit implements Serializable {

    private final UUID id;
    private final UUID parentId;
    private final UUID studentId;
    private final String accessType; // e.g. "VIEW_OVERVIEW", "VIEW_QUIZ_SCORE", "VIEW_COURSE_PROGRESS"
    private final Instant accessedAt;

    public ParentProgressAccessAudit(UUID id, UUID parentId, UUID studentId, String accessType, Instant accessedAt) {
        if (id == null) {
            throw new DomainException("INVALID_ACCESS_AUDIT", "ID không được null");
        }
        if (parentId == null) {
            throw new DomainException("INVALID_ACCESS_AUDIT", "Parent ID không được null");
        }
        if (studentId == null) {
            throw new DomainException("INVALID_ACCESS_AUDIT", "Student ID không được null");
        }
        this.id = id;
        this.parentId = parentId;
        this.studentId = studentId;
        this.accessType = accessType == null ? "VIEW_GENERAL" : accessType;
        this.accessedAt = accessedAt != null ? accessedAt : Instant.now();
    }

    public static ParentProgressAccessAudit record(UUID parentId, UUID studentId, String accessType) {
        return new ParentProgressAccessAudit(UUID.randomUUID(), parentId, studentId, accessType, Instant.now());
    }

    public UUID getId() {
        return id;
    }

    public UUID getParentId() {
        return parentId;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public String getAccessType() {
        return accessType;
    }

    public Instant getAccessedAt() {
        return accessedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParentProgressAccessAudit that = (ParentProgressAccessAudit) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
