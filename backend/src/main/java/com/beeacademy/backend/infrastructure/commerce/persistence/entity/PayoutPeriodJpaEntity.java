package com.beeacademy.backend.infrastructure.commerce.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payout_periods")
public class PayoutPeriodJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "teacher_id", nullable = false)
    private UUID teacherId;

    @Column(name = "month_year", nullable = false, length = 20)
    private String monthYear;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "total_gross_vnd", nullable = false)
    private int totalGrossVnd;

    @Column(name = "total_platform_fee_vnd", nullable = false)
    private int totalPlatformFeeVnd;

    @Column(name = "total_teacher_vnd", nullable = false)
    private int totalTeacherVnd;

    @Column(name = "transaction_count", nullable = false)
    private int transactionCount;

    @Column(name = "paid_by_admin")
    private UUID paidByAdmin;

    @Column(name = "transfer_ref", length = 100)
    private String transferRef;

    @Column(name = "transfer_content", columnDefinition = "TEXT")
    private String transferContent;

    @Column(name = "unc_attachment_url", length = 1000)
    private String uncAttachmentUrl;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public PayoutPeriodJpaEntity() {
    }

    public PayoutPeriodJpaEntity(
            UUID id,
            UUID teacherId,
            String monthYear,
            String status,
            int totalGrossVnd,
            int totalPlatformFeeVnd,
            int totalTeacherVnd,
            int transactionCount,
            UUID paidByAdmin,
            String transferRef,
            String transferContent,
            String uncAttachmentUrl,
            Instant paidAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.teacherId = teacherId;
        this.monthYear = monthYear;
        this.status = status;
        this.totalGrossVnd = totalGrossVnd;
        this.totalPlatformFeeVnd = totalPlatformFeeVnd;
        this.totalTeacherVnd = totalTeacherVnd;
        this.transactionCount = transactionCount;
        this.paidByAdmin = paidByAdmin;
        this.transferRef = transferRef;
        this.transferContent = transferContent;
        this.uncAttachmentUrl = uncAttachmentUrl;
        this.paidAt = paidAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public String getMonthYear() {
        return monthYear;
    }

    public void setMonthYear(String monthYear) {
        this.monthYear = monthYear;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getTotalGrossVnd() {
        return totalGrossVnd;
    }

    public void setTotalGrossVnd(int totalGrossVnd) {
        this.totalGrossVnd = totalGrossVnd;
    }

    public int getTotalPlatformFeeVnd() {
        return totalPlatformFeeVnd;
    }

    public void setTotalPlatformFeeVnd(int totalPlatformFeeVnd) {
        this.totalPlatformFeeVnd = totalPlatformFeeVnd;
    }

    public int getTotalTeacherVnd() {
        return totalTeacherVnd;
    }

    public void setTotalTeacherVnd(int totalTeacherVnd) {
        this.totalTeacherVnd = totalTeacherVnd;
    }

    public int getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(int transactionCount) {
        this.transactionCount = transactionCount;
    }

    public UUID getPaidByAdmin() {
        return paidByAdmin;
    }

    public void setPaidByAdmin(UUID paidByAdmin) {
        this.paidByAdmin = paidByAdmin;
    }

    public String getTransferRef() {
        return transferRef;
    }

    public void setTransferRef(String transferRef) {
        this.transferRef = transferRef;
    }

    public String getTransferContent() {
        return transferContent;
    }

    public void setTransferContent(String transferContent) {
        this.transferContent = transferContent;
    }

    public String getUncAttachmentUrl() {
        return uncAttachmentUrl;
    }

    public void setUncAttachmentUrl(String uncAttachmentUrl) {
        this.uncAttachmentUrl = uncAttachmentUrl;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(Instant paidAt) {
        this.paidAt = paidAt;
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

