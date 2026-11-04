package com.beeacademy.backend.domain.support;

import com.beeacademy.backend.domain.common.AbstractAggregateRoot;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.support.event.ComplaintCreatedDomainEvent;
import com.beeacademy.backend.domain.support.event.ComplaintResolvedDomainEvent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root đại diện cho Khiếu nại / Ticket hỗ trợ người dùng (UC12, UC42).
 * Vòng đời ticket: OPEN -> IN_PROGRESS -> RESOLVED -> CLOSED.
 */
public class Complaint extends AbstractAggregateRoot {

    private final UUID id;
    private final UUID senderId;
    private final String senderRole;
    private final String title;
    private final ComplaintCategory category;
    private ComplaintPriority priority;
    private ComplaintStatus status;
    private final Instant createdAt;
    private Instant lastActivityAt;
    private Instant resolvedAt;
    private UUID resolvedByAdminId;
    private String resolutionSummary;
    private final List<ComplaintMessage> messages = new ArrayList<>();
    private final List<ComplaintAttachment> attachments = new ArrayList<>();

    public Complaint(
            UUID id,
            UUID senderId,
            String senderRole,
            String title,
            ComplaintCategory category,
            ComplaintPriority priority,
            ComplaintStatus status,
            Instant createdAt,
            Instant lastActivityAt,
            Instant resolvedAt,
            UUID resolvedByAdminId,
            String resolutionSummary,
            List<ComplaintMessage> messages,
            List<ComplaintAttachment> attachments
    ) {
        if (id == null) {
            throw new DomainException("INVALID_COMPLAINT", "Complaint ID không được null");
        }
        if (senderId == null) {
            throw new DomainException("INVALID_COMPLAINT", "Sender ID không được null");
        }
        if (title == null || title.isBlank()) {
            throw new DomainException("INVALID_COMPLAINT_TITLE", "Tiêu đề khiếu nại không được để trống");
        }
        this.id = id;
        this.senderId = senderId;
        this.senderRole = senderRole == null ? "STUDENT" : senderRole.toUpperCase();
        this.title = title.trim();
        this.category = category == null ? ComplaintCategory.OTHER : category;
        this.priority = priority == null ? ComplaintPriority.MEDIUM : priority;
        this.status = status == null ? ComplaintStatus.OPEN : status;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.lastActivityAt = lastActivityAt != null ? lastActivityAt : this.createdAt;
        this.resolvedAt = resolvedAt;
        this.resolvedByAdminId = resolvedByAdminId;
        this.resolutionSummary = resolutionSummary;
        if (messages != null) {
            this.messages.addAll(messages);
        }
        if (attachments != null) {
            this.attachments.addAll(attachments);
        }
    }

    public static Complaint open(
            UUID senderId,
            String senderRole,
            String title,
            ComplaintCategory category,
            ComplaintPriority priority,
            String initialMessageContent
    ) {
        if (initialMessageContent == null || initialMessageContent.isBlank()) {
            throw new DomainException("EMPTY_COMPLAINT_MESSAGE", "Nội dung phản ánh không được để trống");
        }
        UUID complaintId = UUID.randomUUID();
        Instant now = Instant.now();

        Complaint complaint = new Complaint(
                complaintId,
                senderId,
                senderRole,
                title,
                category,
                priority,
                ComplaintStatus.OPEN,
                now,
                now,
                null,
                null,
                null,
                null,
                null
        );

        complaint.messages.add(ComplaintMessage.create(complaintId, senderId, senderRole, initialMessageContent));

        complaint.registerDomainEvent(new ComplaintCreatedDomainEvent(
                complaintId, senderId, senderRole, title, complaint.category, complaint.priority
        ));
        return complaint;
    }

    public void addMessage(UUID authorId, String authorRole, String content) {
        if (this.status == ComplaintStatus.CLOSED) {
            throw new DomainException("COMPLAINT_CLOSED", "Khiếu nại đã đóng, không thể gửi thêm tin nhắn");
        }
        Instant now = Instant.now();
        this.messages.add(ComplaintMessage.create(this.id, authorId, authorRole, content));
        this.lastActivityAt = now;

        // Nếu admin trả lời khi đang OPEN -> tự động chuyển sang IN_PROGRESS
        if ("ADMIN".equalsIgnoreCase(authorRole) && this.status == ComplaintStatus.OPEN) {
            this.status = ComplaintStatus.IN_PROGRESS;
        }

        // Nếu người dùng phản hồi sau khi RESOLVED -> mở lại thành IN_PROGRESS
        if (!"ADMIN".equalsIgnoreCase(authorRole) && this.status == ComplaintStatus.RESOLVED) {
            this.status = ComplaintStatus.IN_PROGRESS;
        }
    }

    public void addAttachment(String fileUrl, String fileName, long fileSizeBytes) {
        if (this.status == ComplaintStatus.CLOSED) {
            throw new DomainException("COMPLAINT_CLOSED", "Khiếu nại đã đóng, không thể đính kèm file");
        }
        this.attachments.add(ComplaintAttachment.create(this.id, fileUrl, fileName, fileSizeBytes));
        this.lastActivityAt = Instant.now();
    }

    public void updatePriority(ComplaintPriority newPriority) {
        if (newPriority != null) {
            this.priority = newPriority;
            this.lastActivityAt = Instant.now();
        }
    }

    public void resolve(UUID adminId, String resolutionSummary) {
        if (this.status == ComplaintStatus.CLOSED) {
            throw new DomainException("COMPLAINT_CLOSED", "Khiếu nại đã đóng hoàn toàn");
        }
        if (adminId == null) {
            throw new DomainException("INVALID_ADMIN", "Admin ID giải quyết không được null");
        }
        if (resolutionSummary == null || resolutionSummary.isBlank()) {
            throw new DomainException("EMPTY_RESOLUTION", "Tóm tắt hướng giải quyết không được để trống");
        }

        Instant now = Instant.now();
        this.status = ComplaintStatus.RESOLVED;
        this.resolvedByAdminId = adminId;
        this.resolutionSummary = resolutionSummary.trim();
        this.resolvedAt = now;
        this.lastActivityAt = now;

        registerDomainEvent(new ComplaintResolvedDomainEvent(
                this.id, this.senderId, adminId, this.resolutionSummary, this.resolvedAt
        ));
    }

    public void close() {
        this.status = ComplaintStatus.CLOSED;
        this.lastActivityAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getSenderId() {
        return senderId;
    }

    public String getSenderRole() {
        return senderRole;
    }

    public String getTitle() {
        return title;
    }

    public ComplaintCategory getCategory() {
        return category;
    }

    public ComplaintPriority getPriority() {
        return priority;
    }

    public ComplaintStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getLastActivityAt() {
        return lastActivityAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public UUID getResolvedByAdminId() {
        return resolvedByAdminId;
    }

    public String getResolutionSummary() {
        return resolutionSummary;
    }

    public List<ComplaintMessage> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    public List<ComplaintAttachment> getAttachments() {
        return Collections.unmodifiableList(attachments);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Complaint complaint = (Complaint) o;
        return Objects.equals(id, complaint.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
