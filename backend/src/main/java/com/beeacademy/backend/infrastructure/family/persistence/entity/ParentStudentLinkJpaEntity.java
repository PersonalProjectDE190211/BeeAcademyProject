package com.beeacademy.backend.infrastructure.family.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "parent_student_links")
public class ParentStudentLinkJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "parent_id", nullable = false)
    private UUID parentId;

    @Column(name = "student_id")
    private UUID studentId;

    @Column(name = "student_email", nullable = false)
    private String studentEmail;

    @Column(name = "invite_token", nullable = false, unique = true, length = 128)
    private String inviteToken;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "consent_granted", nullable = false)
    private boolean consentGranted;

    @Column(name = "invite_expires_at", nullable = false)
    private Instant inviteExpiresAt;

    @Column(name = "linked_at")
    private Instant linkedAt;

    @Column(name = "unlinked_at")
    private Instant unlinkedAt;

    @Column(name = "unlinked_by")
    private UUID unlinkedBy;

    @Column(name = "unlink_reason", columnDefinition = "TEXT")
    private String unlinkReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public ParentStudentLinkJpaEntity() {
    }

    public ParentStudentLinkJpaEntity(
            UUID id,
            UUID parentId,
            UUID studentId,
            String studentEmail,
            String inviteToken,
            String status,
            boolean consentGranted,
            Instant inviteExpiresAt,
            Instant linkedAt,
            Instant unlinkedAt,
            UUID unlinkedBy,
            String unlinkReason,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.parentId = parentId;
        this.studentId = studentId;
        this.studentEmail = studentEmail;
        this.inviteToken = inviteToken;
        this.status = status;
        this.consentGranted = consentGranted;
        this.inviteExpiresAt = inviteExpiresAt;
        this.linkedAt = linkedAt;
        this.unlinkedAt = unlinkedAt;
        this.unlinkedBy = unlinkedBy;
        this.unlinkReason = unlinkReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public String getInviteToken() {
        return inviteToken;
    }

    public void setInviteToken(String inviteToken) {
        this.inviteToken = inviteToken;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isConsentGranted() {
        return consentGranted;
    }

    public void setConsentGranted(boolean consentGranted) {
        this.consentGranted = consentGranted;
    }

    public Instant getInviteExpiresAt() {
        return inviteExpiresAt;
    }

    public void setInviteExpiresAt(Instant inviteExpiresAt) {
        this.inviteExpiresAt = inviteExpiresAt;
    }

    public Instant getLinkedAt() {
        return linkedAt;
    }

    public void setLinkedAt(Instant linkedAt) {
        this.linkedAt = linkedAt;
    }

    public Instant getUnlinkedAt() {
        return unlinkedAt;
    }

    public void setUnlinkedAt(Instant unlinkedAt) {
        this.unlinkedAt = unlinkedAt;
    }

    public UUID getUnlinkedBy() {
        return unlinkedBy;
    }

    public void setUnlinkedBy(UUID unlinkedBy) {
        this.unlinkedBy = unlinkedBy;
    }

    public String getUnlinkReason() {
        return unlinkReason;
    }

    public void setUnlinkReason(String unlinkReason) {
        this.unlinkReason = unlinkReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}

