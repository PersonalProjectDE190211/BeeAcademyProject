package com.beeacademy.backend.domain.family;

import com.beeacademy.backend.domain.common.AbstractAggregateRoot;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.family.event.ParentLinkAcceptedDomainEvent;
import com.beeacademy.backend.domain.family.event.ParentLinkInvitedDomainEvent;
import com.beeacademy.backend.domain.family.event.ParentLinkRevokedDomainEvent;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root đại diện cho Liên kết tài khoản Phụ huynh - Học sinh (UC24, UC27, UC28, UC29).
 * Quy tắc nghiệp vụ & Quyền riêng tư:
 * - Phụ huynh gửi lời mời bằng email học sinh (status = PENDING).
 * - Học sinh chấp nhận -> status = ACTIVE, sensitiveDataConsentGranted = true.
 * - Cả Phụ huynh hoặc Học sinh đều có quyền hủy liên kết (UC29).
 * - Khi hủy liên kết -> status = REVOKED, sensitiveDataConsentGranted = false ngay lập tức.
 * - Phụ huynh chỉ được xem dữ liệu học tập khi status = ACTIVE và consent = true.
 */
public class ParentStudentLink extends AbstractAggregateRoot {

    public static final int DEFAULT_INVITE_EXPIRY_HOURS = 72; // 3 ngày

    private final UUID id;
    private final UUID parentId;
    private UUID studentId;
    private final String studentEmail;
    private final String inviteToken;
    private ParentLinkStatus status;
    private boolean sensitiveDataConsentGranted;
    private final Instant inviteExpiresAt;
    private Instant linkedAt;
    private Instant unlinkedAt;
    private UUID unlinkedBy;
    private String unlinkReason;
    private final Instant createdAt;
    private Instant updatedAt;
    private final List<ParentLinkAuditLog> auditLogs = new ArrayList<>();

    public ParentStudentLink(
            UUID id,
            UUID parentId,
            UUID studentId,
            String studentEmail,
            String inviteToken,
            ParentLinkStatus status,
            boolean sensitiveDataConsentGranted,
            Instant inviteExpiresAt,
            Instant linkedAt,
            Instant unlinkedAt,
            UUID unlinkedBy,
            String unlinkReason,
            Instant createdAt,
            Instant updatedAt,
            List<ParentLinkAuditLog> auditLogs
    ) {
        if (id == null) {
            throw new DomainException("INVALID_PARENT_LINK", "Link ID không được null");
        }
        if (parentId == null) {
            throw new DomainException("INVALID_PARENT_LINK", "Parent ID không được null");
        }
        if (studentEmail == null || studentEmail.isBlank()) {
            throw new DomainException("INVALID_STUDENT_EMAIL", "Email học sinh không được để trống");
        }
        this.id = id;
        this.parentId = parentId;
        this.studentId = studentId;
        this.studentEmail = studentEmail.trim().toLowerCase();
        this.inviteToken = inviteToken;
        this.status = status == null ? ParentLinkStatus.PENDING : status;
        this.sensitiveDataConsentGranted = sensitiveDataConsentGranted;
        this.inviteExpiresAt = inviteExpiresAt != null ? inviteExpiresAt : Instant.now().plus(DEFAULT_INVITE_EXPIRY_HOURS, ChronoUnit.HOURS);
        this.linkedAt = linkedAt;
        this.unlinkedAt = unlinkedAt;
        this.unlinkedBy = unlinkedBy;
        this.unlinkReason = unlinkReason;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
        if (auditLogs != null) {
            this.auditLogs.addAll(auditLogs);
        }
    }

    public static ParentStudentLink invite(UUID parentId, String studentEmail, String token, int expiryHours) {
        UUID linkId = UUID.randomUUID();
        Instant now = Instant.now();
        int hours = expiryHours <= 0 ? DEFAULT_INVITE_EXPIRY_HOURS : expiryHours;
        Instant expires = now.plus(hours, ChronoUnit.HOURS);

        ParentStudentLink link = new ParentStudentLink(
                linkId,
                parentId,
                null,
                studentEmail,
                token,
                ParentLinkStatus.PENDING,
                false,
                expires,
                null,
                null,
                null,
                null,
                now,
                now,
                null
        );

        link.auditLogs.add(ParentLinkAuditLog.create(
                linkId, parentId, "INVITED", "Phụ huynh gửi lời mời liên kết đến email: " + studentEmail
        ));

        link.registerDomainEvent(new ParentLinkInvitedDomainEvent(linkId, parentId, studentEmail, token, expires));
        return link;
    }

    public void accept(UUID confirmedStudentId, Instant now) {
        if (this.status != ParentLinkStatus.PENDING) {
            throw new DomainException("INVALID_LINK_STATE", "Lời mời liên kết không ở trạng thái PENDING");
        }
        Instant check = now != null ? now : Instant.now();
        if (check.isAfter(this.inviteExpiresAt)) {
            this.status = ParentLinkStatus.REJECTED;
            this.updatedAt = check;
            throw new DomainException("INVITE_EXPIRED", "Lời mời liên kết đã hết hạn");
        }
        if (confirmedStudentId == null) {
            throw new DomainException("INVALID_STUDENT_ID", "Student ID không được null khi chấp nhận liên kết");
        }

        this.studentId = confirmedStudentId;
        this.status = ParentLinkStatus.ACTIVE;
        this.sensitiveDataConsentGranted = true;
        this.linkedAt = check;
        this.updatedAt = check;

        this.auditLogs.add(ParentLinkAuditLog.create(
                this.id, confirmedStudentId, "ACCEPTED", "Học sinh chấp thuận liên kết và cấp quyền theo dõi tiến độ"
        ));

        registerDomainEvent(new ParentLinkAcceptedDomainEvent(this.id, this.parentId, this.studentId, this.linkedAt));
    }

    public void reject(UUID studentId, String reason) {
        if (this.status != ParentLinkStatus.PENDING) {
            throw new DomainException("INVALID_LINK_STATE", "Chỉ có thể từ chối lời mời khi đang ở trạng thái PENDING");
        }
        Instant now = Instant.now();
        this.status = ParentLinkStatus.REJECTED;
        this.updatedAt = now;

        this.auditLogs.add(ParentLinkAuditLog.create(
                this.id, studentId, "REJECTED", "Học sinh từ chối liên kết. Lý do: " + (reason != null ? reason : "Không có")
        ));
    }

    /**
     * Hủy liên kết tài khoản (UC29). Cả Phụ huynh hoặc Học sinh đều có quyền gọi hàm này.
     */
    public void unlink(UUID actorId, String reason) {
        if (actorId == null) {
            throw new DomainException("INVALID_ACTOR", "Actor ID không được null");
        }
        boolean isParent = Objects.equals(this.parentId, actorId);
        boolean isStudent = this.studentId != null && Objects.equals(this.studentId, actorId);
        if (!isParent && !isStudent) {
            throw new DomainException("UNAUTHORIZED_UNLINK", "Chỉ Phụ huynh hoặc Học sinh trong mối liên kết mới có quyền hủy");
        }
        if (this.status == ParentLinkStatus.REVOKED) {
            return; // Idempotent
        }
        if (this.status != ParentLinkStatus.ACTIVE && this.status != ParentLinkStatus.PENDING) {
            throw new DomainException("CANNOT_UNLINK", "Không thể hủy liên kết ở trạng thái hiện tại: " + this.status);
        }

        Instant now = Instant.now();
        this.status = ParentLinkStatus.REVOKED;
        this.sensitiveDataConsentGranted = false; // Ngay lập tức thu hồi quyền xem dữ liệu
        this.unlinkedAt = now;
        this.unlinkedBy = actorId;
        this.unlinkReason = reason != null ? reason.trim() : "Người dùng chủ động hủy liên kết";
        this.updatedAt = now;

        this.auditLogs.add(ParentLinkAuditLog.create(
                this.id, actorId, "REVOKED",
                (isParent ? "Phụ huynh" : "Học sinh") + " hủy liên kết. Lý do: " + this.unlinkReason
        ));

        registerDomainEvent(new ParentLinkRevokedDomainEvent(
                this.id, this.parentId, this.studentId, actorId, this.unlinkReason
        ));
    }

    /**
     * Kiểm tra quyền của phụ huynh đối với việc xem tiến độ/điểm thi của con (UC24).
     */
    public boolean isDataAccessibleByParent(UUID queryingParentId) {
        return this.status == ParentLinkStatus.ACTIVE
                && Objects.equals(this.parentId, queryingParentId)
                && this.sensitiveDataConsentGranted;
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

    public String getStudentEmail() {
        return studentEmail;
    }

    public String getInviteToken() {
        return inviteToken;
    }

    public ParentLinkStatus getStatus() {
        return status;
    }

    public boolean isSensitiveDataConsentGranted() {
        return sensitiveDataConsentGranted;
    }

    public Instant getInviteExpiresAt() {
        return inviteExpiresAt;
    }

    public Instant getLinkedAt() {
        return linkedAt;
    }

    public Instant getUnlinkedAt() {
        return unlinkedAt;
    }

    public UUID getUnlinkedBy() {
        return unlinkedBy;
    }

    public String getUnlinkReason() {
        return unlinkReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<ParentLinkAuditLog> getAuditLogs() {
        return Collections.unmodifiableList(auditLogs);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParentStudentLink that = (ParentStudentLink) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
