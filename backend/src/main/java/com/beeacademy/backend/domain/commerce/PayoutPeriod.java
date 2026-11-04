package com.beeacademy.backend.domain.commerce;

import com.beeacademy.backend.domain.commerce.event.PayoutConfirmedDomainEvent;
import com.beeacademy.backend.domain.common.AbstractAggregateRoot;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.common.MoneyVnd;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root đại diện cho Kỳ thanh toán đối soát doanh thu cho Giáo viên (UC37, UC43).
 * Chỉ Admin mới có quyền xác nhận chuyển khoản và đóng kỳ thanh toán.
 */
public class PayoutPeriod extends AbstractAggregateRoot {

    private final UUID id;
    private final UUID teacherId;
    private final String monthYear; // định dạng "YYYY-MM"
    private PayoutStatus status;
    private MoneyVnd totalGross;
    private MoneyVnd totalPlatformFee;
    private MoneyVnd totalTeacherAmount;
    private int transactionCount;
    private Instant paidAt;
    private UUID paidByAdmin;
    private String transferRef;
    private String transferContent;
    private String uncAttachmentUrl;
    private final Instant createdAt;
    private Instant updatedAt;

    public PayoutPeriod(
            UUID id,
            UUID teacherId,
            String monthYear,
            PayoutStatus status,
            MoneyVnd totalGross,
            MoneyVnd totalPlatformFee,
            MoneyVnd totalTeacherAmount,
            int transactionCount,
            Instant paidAt,
            UUID paidByAdmin,
            String transferRef,
            String transferContent,
            String uncAttachmentUrl,
            Instant createdAt,
            Instant updatedAt
    ) {
        if (id == null) {
            throw new DomainException("INVALID_PAYOUT_PERIOD", "ID không được để trống");
        }
        if (teacherId == null) {
            throw new DomainException("INVALID_PAYOUT_PERIOD", "Teacher ID không được để trống");
        }
        if (monthYear == null || !monthYear.matches("^\\d{4}-(0[1-9]|1[0-2])$")) {
            throw new DomainException("INVALID_MONTH_YEAR", "Kỳ thanh toán phải có định dạng YYYY-MM");
        }
        this.id = id;
        this.teacherId = teacherId;
        this.monthYear = monthYear;
        this.status = status == null ? PayoutStatus.PENDING : status;
        this.totalGross = totalGross == null ? MoneyVnd.ZERO : totalGross;
        this.totalPlatformFee = totalPlatformFee == null ? MoneyVnd.ZERO : totalPlatformFee;
        this.totalTeacherAmount = totalTeacherAmount == null ? MoneyVnd.ZERO : totalTeacherAmount;
        this.transactionCount = Math.max(0, transactionCount);
        this.paidAt = paidAt;
        this.paidByAdmin = paidByAdmin;
        this.transferRef = transferRef;
        this.transferContent = transferContent;
        this.uncAttachmentUrl = uncAttachmentUrl;
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
    }

    public static PayoutPeriod create(UUID teacherId, String monthYear) {
        Instant now = Instant.now();
        return new PayoutPeriod(
                UUID.randomUUID(),
                teacherId,
                monthYear,
                PayoutStatus.PENDING,
                MoneyVnd.ZERO,
                MoneyVnd.ZERO,
                MoneyVnd.ZERO,
                0,
                null,
                null,
                null,
                null,
                null,
                now,
                now
        );
    }

    public void addSplit(RevenueSplit split) {
        if (this.status == PayoutStatus.PAID) {
            throw new DomainException("PAYOUT_ALREADY_PAID", "Kỳ thanh toán đã được chi trả, không thể thêm giao dịch mới");
        }
        if (!this.teacherId.equals(split.getTeacherId())) {
            throw new DomainException("TEACHER_MISMATCH", "Giao dịch không thuộc giáo viên của kỳ thanh toán này");
        }
        this.totalGross = this.totalGross.plus(split.getGrossAmount());
        this.totalPlatformFee = this.totalPlatformFee.plus(split.getPlatformFee());
        this.totalTeacherAmount = this.totalTeacherAmount.plus(split.getTeacherAmount());
        this.transactionCount++;
        this.updatedAt = Instant.now();
    }

    public void startProcessing() {
        if (this.status != PayoutStatus.PENDING) {
            throw new DomainException("INVALID_PAYOUT_STATE", "Chỉ kỳ thanh toán PENDING mới có thể chuyển sang PROCESSING");
        }
        this.status = PayoutStatus.PROCESSING;
        this.updatedAt = Instant.now();
    }

    public void confirmPayout(
            UUID adminId,
            String transferRef,
            String transferContent,
            String uncAttachmentUrl,
            Instant payoutTime
    ) {
        if (this.status == PayoutStatus.PAID) {
            return; // Idempotent
        }
        if (this.status != PayoutStatus.PENDING && this.status != PayoutStatus.PROCESSING) {
            throw new DomainException("INVALID_PAYOUT_STATE", "Kỳ thanh toán không ở trạng thái sẵn sàng chi trả");
        }
        if (adminId == null) {
            throw new DomainException("INVALID_ADMIN", "Admin ID xác nhận thanh toán không được null");
        }
        if (transferRef == null || transferRef.isBlank()) {
            throw new DomainException("MISSING_TRANSFER_REF", "Mã giao dịch ngân hàng (transferRef) không được để trống");
        }
        if (this.totalTeacherAmount.isZero()) {
            throw new DomainException("ZERO_PAYOUT_AMOUNT", "Số tiền chi trả cho giáo viên bằng 0, không cần chuyển khoản");
        }

        Instant now = payoutTime != null ? payoutTime : Instant.now();
        this.status = PayoutStatus.PAID;
        this.paidByAdmin = adminId;
        this.transferRef = transferRef.trim();
        this.transferContent = transferContent;
        this.uncAttachmentUrl = uncAttachmentUrl;
        this.paidAt = now;
        this.updatedAt = now;

        registerDomainEvent(new PayoutConfirmedDomainEvent(
                this.id,
                this.teacherId,
                this.monthYear,
                this.totalTeacherAmount,
                this.transferRef,
                this.paidByAdmin
        ));
    }

    public void reject(UUID adminId, String reason) {
        if (this.status == PayoutStatus.PAID) {
            throw new DomainException("CANNOT_REJECT_PAID", "Kỳ thanh toán đã chi trả thành công, không thể từ chối");
        }
        this.status = PayoutStatus.REJECTED;
        this.paidByAdmin = adminId;
        this.transferContent = reason;
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getTeacherId() {
        return teacherId;
    }

    public String getMonthYear() {
        return monthYear;
    }

    public PayoutStatus getStatus() {
        return status;
    }

    public MoneyVnd getTotalGross() {
        return totalGross;
    }

    public MoneyVnd getTotalPlatformFee() {
        return totalPlatformFee;
    }

    public MoneyVnd getTotalTeacherAmount() {
        return totalTeacherAmount;
    }

    public int getTransactionCount() {
        return transactionCount;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public UUID getPaidByAdmin() {
        return paidByAdmin;
    }

    public String getTransferRef() {
        return transferRef;
    }

    public String getTransferContent() {
        return transferContent;
    }

    public String getUncAttachmentUrl() {
        return uncAttachmentUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PayoutPeriod that = (PayoutPeriod) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
