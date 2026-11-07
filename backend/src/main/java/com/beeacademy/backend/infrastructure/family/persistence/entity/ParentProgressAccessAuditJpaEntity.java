package com.beeacademy.backend.infrastructure.family.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "parent_progress_access_audits")
public class ParentProgressAccessAuditJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "parent_id", nullable = false)
    private UUID parentId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "access_type", nullable = false, length = 50)
    private String accessType;

    @Column(name = "accessed_at", nullable = false)
    private Instant accessedAt;

    public ParentProgressAccessAuditJpaEntity() {
    }

    public ParentProgressAccessAuditJpaEntity(UUID id, UUID parentId, UUID studentId, String accessType, Instant accessedAt) {
        this.id = id;
        this.parentId = parentId;
        this.studentId = studentId;
        this.accessType = accessType;
        this.accessedAt = accessedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getParentId() {
        return parentId;
    }

    public void setParentId(UUID parentId) {
        this.parentId = parentId;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public void setStudentId(UUID studentId) {
        this.studentId = studentId;
    }

    public String getAccessType() {
        return accessType;
    }

    public void setAccessType(String accessType) {
        this.accessType = accessType;
    }

    public Instant getAccessedAt() {
        return accessedAt;
    }

    public void setAccessedAt(Instant accessedAt) {
        this.accessedAt = accessedAt;
    }
}

