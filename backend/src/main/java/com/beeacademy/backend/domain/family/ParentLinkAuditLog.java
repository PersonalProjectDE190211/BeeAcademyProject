package com.beeacademy.backend.domain.family;

import com.beeacademy.backend.domain.common.DomainException;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity ghi lại vết kiểm toán vòng đời liên kết Phụ huynh - Học sinh (UC27, UC28, UC29).
 */
public class ParentLinkAuditLog implements Serializable {

    private final UUID id;
    private final UUID linkId;
    private final UUID actorId;
    private final String action; // INVITED, ACCEPTED, REJECTED, REVOKED
    private final String details;
    private final Instant occurredAt;

    public ParentLinkAuditLog(UUID id, UUID linkId, UUID actorId, String action, String details, Instant occurredAt) {
        if (id == null) {
            throw new DomainException("INVALID_AUDIT_LOG", "ID không được null");
        }
        if (linkId == null) {
            throw new DomainException("INVALID_AUDIT_LOG", "Link ID không được null");
        }
        this.id = id;
        this.linkId = linkId;
        this.actorId = actorId;
        this.action = action == null ? "UNKNOWN" : action;
        this.details = details == null ? "" : details;
        this.occurredAt = occurredAt != null ? occurredAt : Instant.now();
    }

    public static ParentLinkAuditLog create(UUID linkId, UUID actorId, String action, String details) {
        return new ParentLinkAuditLog(UUID.randomUUID(), linkId, actorId, action, details, Instant.now());
    }

    public UUID getId() {
        return id;
    }

    public UUID getLinkId() {
        return linkId;
    }

    public UUID getActorId() {
        return actorId;
    }

    public String getAction() {
        return action;
    }

    public String getDetails() {
        return details;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParentLinkAuditLog that = (ParentLinkAuditLog) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
