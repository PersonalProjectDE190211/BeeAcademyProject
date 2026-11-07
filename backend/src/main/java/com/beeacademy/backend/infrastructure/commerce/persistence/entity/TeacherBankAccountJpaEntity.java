package com.beeacademy.backend.infrastructure.commerce.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "teacher_bank_accounts")
public class TeacherBankAccountJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "teacher_id", nullable = false, unique = true)
    private UUID teacherId;

    @Column(name = "bank_name", nullable = false, length = 100)
    private String bankName;

    @Column(name = "account_number_encrypted", nullable = false, length = 500)
    private String accountNumberEncrypted;

    @Column(name = "account_number_masked", nullable = false, length = 30)
    private String accountNumberMasked;

    @Column(name = "account_holder", nullable = false, length = 150)
    private String accountHolder;

    @Column(name = "branch", length = 150)
    private String branch;

    @Column(name = "verify_status", nullable = false, length = 20)
    private String verifyStatus;

    @Column(name = "otp_verified", nullable = false)
    private boolean otpVerified;

    @Column(name = "verified_by_admin")
    private UUID verifiedByAdmin;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public TeacherBankAccountJpaEntity() {
    }

    public TeacherBankAccountJpaEntity(
            UUID id,
            UUID teacherId,
            String bankName,
            String accountNumberEncrypted,
            String accountNumberMasked,
            String accountHolder,
            String branch,
            String verifyStatus,
            boolean otpVerified,
            UUID verifiedByAdmin,
            Instant verifiedAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.teacherId = teacherId;
        this.bankName = bankName;
        this.accountNumberEncrypted = accountNumberEncrypted;
        this.accountNumberMasked = accountNumberMasked;
        this.accountHolder = accountHolder;
        this.branch = branch;
        this.verifyStatus = verifyStatus;
        this.otpVerified = otpVerified;
        this.verifiedByAdmin = verifiedByAdmin;
        this.verifiedAt = verifiedAt;
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

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountNumberEncrypted() {
        return accountNumberEncrypted;
    }

    public void setAccountNumberEncrypted(String accountNumberEncrypted) {
        this.accountNumberEncrypted = accountNumberEncrypted;
    }

    public String getAccountNumberMasked() {
        return accountNumberMasked;
    }

    public void setAccountNumberMasked(String accountNumberMasked) {
        this.accountNumberMasked = accountNumberMasked;
    }

    public String getAccountHolder() {
        return accountHolder;
    }

    public void setAccountHolder(String accountHolder) {
        this.accountHolder = accountHolder;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getVerifyStatus() {
        return verifyStatus;
    }

    public void setVerifyStatus(String verifyStatus) {
        this.verifyStatus = verifyStatus;
    }

    public boolean isOtpVerified() {
        return otpVerified;
    }

    public void setOtpVerified(boolean otpVerified) {
        this.otpVerified = otpVerified;
    }

    public UUID getVerifiedByAdmin() {
        return verifiedByAdmin;
    }

    public void setVerifiedByAdmin(UUID verifiedByAdmin) {
        this.verifiedByAdmin = verifiedByAdmin;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(Instant verifiedAt) {
        this.verifiedAt = verifiedAt;
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

