package com.beeacademy.backend.domain.commerce;

import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.common.MoneyVnd;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity đại diện cho Voucher thưởng giảm giá khi mua khóa học.
 * Thưởng cho học sinh đạt thành tích xuất sắc trong các bài thi hoặc chương trình khích lệ.
 */
public class RewardVoucher implements Serializable {

    private final UUID id;
    private final String code;
    private final String title;
    private final int discountPercent;
    private final MoneyVnd maxDiscountAmount;
    private RewardVoucherStatus status;
    private final UUID assignedStudentId; // Nếu null: voucher chung; nếu có: chỉ học sinh này được dùng
    private final Instant validFrom;
    private final Instant validTo;
    private final Instant createdAt;
    private Instant usedAt;

    public RewardVoucher(
            UUID id,
            String code,
            String title,
            int discountPercent,
            MoneyVnd maxDiscountAmount,
            RewardVoucherStatus status,
            UUID assignedStudentId,
            Instant validFrom,
            Instant validTo,
            Instant createdAt,
            Instant usedAt
    ) {
        if (id == null) {
            throw new DomainException("INVALID_VOUCHER", "ID không được null");
        }
        if (code == null || code.isBlank()) {
            throw new DomainException("INVALID_VOUCHER_CODE", "Mã voucher không được để trống");
        }
        if (discountPercent <= 0 || discountPercent > 100) {
            throw new DomainException("INVALID_DISCOUNT_PERCENT", "Phần trăm giảm giá phải từ 1% đến 100%");
        }
        this.id = id;
        this.code = code.trim().toUpperCase();
        this.title = title == null ? "" : title.trim();
        this.discountPercent = discountPercent;
        this.maxDiscountAmount = maxDiscountAmount != null ? maxDiscountAmount : MoneyVnd.ZERO;
        this.status = status == null ? RewardVoucherStatus.ACTIVE : status;
        this.assignedStudentId = assignedStudentId;
        this.validFrom = validFrom != null ? validFrom : Instant.now();
        this.validTo = validTo;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.usedAt = usedAt;
    }

    public static RewardVoucher createForStudent(
            UUID studentId,
            String code,
            String title,
            int discountPercent,
            MoneyVnd maxDiscountAmount,
            Instant validTo
    ) {
        return new RewardVoucher(
                UUID.randomUUID(),
                code,
                title,
                discountPercent,
                maxDiscountAmount,
                RewardVoucherStatus.ACTIVE,
                studentId,
                Instant.now(),
                validTo,
                Instant.now(),
                null
        );
    }

    public boolean isValid(Instant now) {
        if (this.status != RewardVoucherStatus.ACTIVE) {
            return false;
        }
        Instant checkTime = now != null ? now : Instant.now();
        if (checkTime.isBefore(this.validFrom)) {
            return false;
        }
        return this.validTo == null || !checkTime.isAfter(this.validTo);
    }

    public MoneyVnd calculateDiscount(MoneyVnd orderTotal) {
        if (orderTotal == null || orderTotal.isZero()) {
            return MoneyVnd.ZERO;
        }
        MoneyVnd calculated = orderTotal.applyDiscountPercent(this.discountPercent);
        if (!this.maxDiscountAmount.isZero() && calculated.isGreaterThan(this.maxDiscountAmount)) {
            return this.maxDiscountAmount;
        }
        return calculated;
    }

    public void use(UUID studentId, Instant useTime) {
        Instant check = useTime != null ? useTime : Instant.now();
        if (!isValid(check)) {
            throw new DomainException("VOUCHER_NOT_USABLE", "Voucher không còn hiệu lực hoặc đã hết hạn");
        }
        if (this.assignedStudentId != null && !this.assignedStudentId.equals(studentId)) {
            throw new DomainException("VOUCHER_STUDENT_MISMATCH", "Voucher này không thuộc về học sinh hiện tại");
        }
        this.status = RewardVoucherStatus.USED;
        this.usedAt = check;
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public int getDiscountPercent() {
        return discountPercent;
    }

    public MoneyVnd getMaxDiscountAmount() {
        return maxDiscountAmount;
    }

    public RewardVoucherStatus getStatus() {
        return status;
    }

    public UUID getAssignedStudentId() {
        return assignedStudentId;
    }

    public Instant getValidFrom() {
        return validFrom;
    }

    public Instant getValidTo() {
        return validTo;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUsedAt() {
        return usedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RewardVoucher that = (RewardVoucher) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

