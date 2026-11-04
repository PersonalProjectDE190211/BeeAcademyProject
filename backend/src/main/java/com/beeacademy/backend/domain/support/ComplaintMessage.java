package com.beeacademy.backend.domain.support;

import com.beeacademy.backend.domain.common.DomainException;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity đại diện cho một tin nhắn trao đổi trong luồng khiếu nại (Complaint Thread).
 */
public class ComplaintMessage implements Serializable {

    private final UUID id;
    private final UUID complaintId;
    private final UUID authorId;
    private final String authorRole; // STUDENT, PARENT, TEACHER, ADMIN
    private final String content;
    private final Instant createdAt;

    public ComplaintMessage(UUID id, UUID complaintId, UUID authorId, String authorRole, String content, Instant createdAt) {
        if (id == null) {
            throw new DomainException("INVALID_MESSAGE", "ID tin nhắn không được null");
        }
        if (complaintId == null) {
            throw new DomainException("INVALID_MESSAGE", "Complaint ID không được null");
        }
        if (authorId == null) {
            throw new DomainException("INVALID_MESSAGE", "Tác giả tin nhắn không được null");
        }
        if (content == null || content.isBlank()) {
            throw new DomainException("INVALID_MESSAGE", "Nội dung tin nhắn không được để trống");
        }
        this.id = id;
        this.complaintId = complaintId;
        this.authorId = authorId;
        this.authorRole = authorRole == null ? "USER" : authorRole.toUpperCase();
        this.content = content.trim();
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public static ComplaintMessage create(UUID complaintId, UUID authorId, String authorRole, String content) {
        return new ComplaintMessage(UUID.randomUUID(), complaintId, authorId, authorRole, content, Instant.now());
    }

    public UUID getId() {
        return id;
    }

    public UUID getComplaintId() {
        return complaintId;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public String getAuthorRole() {
        return authorRole;
    }

    public String getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComplaintMessage that = (ComplaintMessage) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
