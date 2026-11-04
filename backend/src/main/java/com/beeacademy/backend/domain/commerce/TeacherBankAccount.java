package com.beeacademy.backend.domain.commerce;

import com.beeacademy.backend.domain.commerce.event.TeacherBankUpdatedDomainEvent;
import com.beeacademy.backend.domain.common.AbstractAggregateRoot;
import com.beeacademy.backend.domain.common.DomainException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root đại diện cho Tài khoản ngân hàng nhận tiền quyết toán của Giáo viên.
 * Bảo mật: Mọi thay đổi thông tin STK phải được ghi vết kiểm toán (TeacherBankAuditLog)
 * và đưa trạng thái về PENDING để chờ xác thực hoặc yêu cầu OTP.
 */
public class TeacherBankAccount extends AbstractAggregateRoot {

    private final UUID id;
    private final UUID teacherId;
    private String bankName;
    private String accountNumber;
    private String accountHolder;
    private String branch;
    private BankVerifyStatus verifyStatus;
    private boolean otpVerified;
    private final Instant createdAt;
    private Instant updatedAt;
    private final List<TeacherBankAuditLog> auditLogs = new ArrayList<>();

    public TeacherBankAccount(
            UUID id,
            UUID teacherId,
            String bankName,
            String accountNumber,
            String accountHolder,
            String branch,
            BankVerifyStatus verifyStatus,
            boolean otpVerified,
            Instant createdAt,
            Instant updatedAt,
            List<TeacherBankAuditLog> auditLogs
    ) {
        if (id == null) {
            throw new DomainException("INVALID_BANK_ACCOUNT", "ID không được để trống");
        }
        if (teacherId == null) {
            throw new DomainException("INVALID_BANK_ACCOUNT", "Teacher ID không được để trống");
        }
        if (bankName == null || bankName.isBlank()) {
            throw new DomainException("INVALID_BANK_NAME", "Tên ngân hàng không được để trống");
        }
        if (accountNumber == null || accountNumber.isBlank() || !accountNumber.matches("^[0-9A-Za-z]{6,30}$")) {
            throw new DomainException("INVALID_ACCOUNT_NUMBER", "Số tài khoản ngân hàng không hợp lệ");
        }
        if (accountHolder == null || accountHolder.isBlank()) {
            throw new DomainException("INVALID_ACCOUNT_HOLDER", "Tên chủ tài khoản không được để trống");
        }

        this.id = id;
        this.teacherId = teacherId;
        this.bankName = bankName.trim();
        this.accountNumber = accountNumber.trim();
        this.accountHolder = accountHolder.trim().toUpperCase();
        this.branch = branch != null ? branch.trim() : "";
        this.verifyStatus = verifyStatus == null ? BankVerifyStatus.PENDING : verifyStatus;
        this.otpVerified = otpVerified;
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
        if (auditLogs != null) {
            this.auditLogs.addAll(auditLogs);
        }
    }

    public static TeacherBankAccount create(
            UUID teacherId,
            String bankName,
            String accountNumber,
            String accountHolder,
            String branch
    ) {
        UUID accountId = UUID.randomUUID();
        Instant now = Instant.now();
        TeacherBankAccount account = new TeacherBankAccount(
                accountId,
                teacherId,
                bankName,
                accountNumber,
                accountHolder,
                branch,
                BankVerifyStatus.PENDING,
                false,
                now,
                now,
                null
        );
        account.auditLogs.add(TeacherBankAuditLog.create(
                teacherId,
                "TEACHER",
                "Tạo mới thông tin tài khoản ngân hàng",
                String.format("{\"bankName\":\"%s\",\"accountNumber\":\"%s\",\"accountHolder\":\"%s\"}",
                        account.bankName, account.accountNumber, account.accountHolder)
        ));
        account.registerDomainEvent(new TeacherBankUpdatedDomainEvent(
                accountId, teacherId, account.bankName, account.accountNumber, account.accountHolder
        ));
        return account;
    }

    public void updateAccountDetails(
            String newBankName,
            String newAccountNumber,
            String newAccountHolder,
            String newBranch,
            String changedByName,
            String reason
    ) {
        if (newBankName == null || newBankName.isBlank()) {
            throw new DomainException("INVALID_BANK_NAME", "Tên ngân hàng mới không được rỗng");
        }
        if (newAccountNumber == null || newAccountNumber.isBlank() || !newAccountNumber.matches("^[0-9A-Za-z]{6,30}$")) {
            throw new DomainException("INVALID_ACCOUNT_NUMBER", "Số tài khoản mới không hợp lệ");
        }
        if (newAccountHolder == null || newAccountHolder.isBlank()) {
            throw new DomainException("INVALID_ACCOUNT_HOLDER", "Tên chủ tài khoản mới không được rỗng");
        }

        String oldDetails = String.format("{\"oldBank\":\"%s\",\"oldAcc\":\"%s\"}", this.bankName, this.accountNumber);
        String newDetails = String.format("{\"newBank\":\"%s\",\"newAcc\":\"%s\"}", newBankName.trim(), newAccountNumber.trim());

        this.bankName = newBankName.trim();
        this.accountNumber = newAccountNumber.trim();
        this.accountHolder = newAccountHolder.trim().toUpperCase();
        this.branch = newBranch != null ? newBranch.trim() : "";
        this.verifyStatus = BankVerifyStatus.PENDING; // Phải thẩm định lại khi đổi số tài khoản
        this.otpVerified = false;
        this.updatedAt = Instant.now();

        this.auditLogs.add(TeacherBankAuditLog.create(
                this.teacherId,
                changedByName,
                reason != null ? reason : "Cập nhật thông tin tài khoản",
                oldDetails + " -> " + newDetails
        ));

        registerDomainEvent(new TeacherBankUpdatedDomainEvent(
                this.id, this.teacherId, this.bankName, this.accountNumber, this.accountHolder
        ));
    }

    public void verifyByAdmin(UUID adminId) {
        if (adminId == null) {
            throw new DomainException("INVALID_ADMIN", "Admin ID xác nhận tài khoản không được null");
        }
        this.verifyStatus = BankVerifyStatus.VERIFIED;
        this.updatedAt = Instant.now();
        this.auditLogs.add(TeacherBankAuditLog.create(
                this.teacherId,
                "ADMIN_" + adminId,
                "Admin thẩm định và xác nhận tài khoản ngân hàng hợp lệ",
                "{\"status\":\"VERIFIED\"}"
        ));
    }

    public void verifyWithOtp() {
        this.otpVerified = true;
        this.updatedAt = Instant.now();
    }

    public void rejectByAdmin(UUID adminId, String reason) {
        if (adminId == null) {
            throw new DomainException("INVALID_ADMIN", "Admin ID không được null");
        }
        this.verifyStatus = BankVerifyStatus.REJECTED;
        this.updatedAt = Instant.now();
        this.auditLogs.add(TeacherBankAuditLog.create(
                this.teacherId,
                "ADMIN_" + adminId,
                "Admin từ chối xác thực tài khoản: " + reason,
                "{\"status\":\"REJECTED\"}"
        ));
    }

    public UUID getId() {
        return id;
    }

    public UUID getTeacherId() {
        return teacherId;
    }

    public String getBankName() {
        return bankName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getAccountHolder() {
        return accountHolder;
    }

    public String getBranch() {
        return branch;
    }

    public BankVerifyStatus getVerifyStatus() {
        return verifyStatus;
    }

    public boolean isOtpVerified() {
        return otpVerified;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<TeacherBankAuditLog> getAuditLogs() {
        return Collections.unmodifiableList(auditLogs);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeacherBankAccount that = (TeacherBankAccount) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
