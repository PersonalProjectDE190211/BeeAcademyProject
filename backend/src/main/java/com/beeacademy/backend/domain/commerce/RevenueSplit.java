package com.beeacademy.backend.domain.commerce;

import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.common.MoneyVnd;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity đại diện cho việc phân chia doanh thu từng đơn hàng (Double-Entry Ledger Invariant):
 * - Mặc định: 80% Giáo viên / 20% Nền tảng (Platform fee).
 * - Bất biến: grossAmount == platformFee + teacherAmount.
 */
public class RevenueSplit implements Serializable {

    public static final int DEFAULT_PLATFORM_FEE_PERCENT = 20;
    public static final int DEFAULT_TEACHER_PERCENT = 80;

    private static final DateTimeFormatter PERIOD_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM")
            .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));

    private final UUID id;
    private final UUID teacherId;
    private final UUID studentId;
    private final UUID courseId;
    private final UUID orderId;
    private final UUID orderItemId;
    private final MoneyVnd grossAmount;
    private final MoneyVnd platformFee;
    private final MoneyVnd teacherAmount;
    private final int platformFeePercent;
    private final int teacherPercent;
    private final String period; // định dạng "YYYY-MM"
    private UUID payoutPeriodId;
    private final Instant occurredAt;

    public RevenueSplit(
            UUID id,
            UUID teacherId,
            UUID studentId,
            UUID courseId,
            UUID orderId,
            UUID orderItemId,
            MoneyVnd grossAmount,
            MoneyVnd platformFee,
            MoneyVnd teacherAmount,
            int platformFeePercent,
            int teacherPercent,
            String period,
            UUID payoutPeriodId,
            Instant occurredAt
    ) {
        if (id == null) {
            throw new DomainException("INVALID_REVENUE_SPLIT", "ID không được để trống");
        }
        if (teacherId == null) {
            throw new DomainException("INVALID_REVENUE_SPLIT", "Teacher ID không được để trống");
        }
        if (orderId == null || orderItemId == null) {
            throw new DomainException("INVALID_REVENUE_SPLIT", "Thông tin đơn hàng không hợp lệ");
        }
        if (grossAmount == null || platformFee == null || teacherAmount == null) {
            throw new DomainException("INVALID_REVENUE_SPLIT", "Số tiền phân chia không được null");
        }
        if (platformFeePercent + teacherPercent != 100) {
            throw new DomainException("INVALID_REVENUE_RATIO", "Tổng tỷ lệ phân chia phải bằng 100%");
        }
        // Kiểm tra tính toàn vẹn: gross == fee + teacher
        if (grossAmount.amount() != platformFee.amount() + teacherAmount.amount()) {
            throw new DomainException("REVENUE_SPLIT_MISMATCH",
                    String.format("Doanh thu không khớp: Tổng %s != Phí sàn %s + Tiền GV %s",
                            grossAmount, platformFee, teacherAmount));
        }

        this.id = id;
        this.teacherId = teacherId;
        this.studentId = studentId;
        this.courseId = courseId;
        this.orderId = orderId;
        this.orderItemId = orderItemId;
        this.grossAmount = grossAmount;
        this.platformFee = platformFee;
        this.teacherAmount = teacherAmount;
        this.platformFeePercent = platformFeePercent;
        this.teacherPercent = teacherPercent;
        this.period = period != null ? period : PERIOD_FORMATTER.format(occurredAt != null ? occurredAt : Instant.now());
        this.payoutPeriodId = payoutPeriodId;
        this.occurredAt = occurredAt != null ? occurredAt : Instant.now();
    }

    public static RevenueSplit calculate(
            UUID teacherId,
            UUID studentId,
            UUID courseId,
            UUID orderId,
            UUID orderItemId,
            MoneyVnd grossAmount,
            int platformFeePercent,
            Instant occurredAt
    ) {
        if (platformFeePercent < 0 || platformFeePercent > 100) {
            throw new DomainException("INVALID_FEE_PERCENT", "Tỷ lệ phí nền tảng phải từ 0 đến 100%");
        }
        int teacherPct = 100 - platformFeePercent;
        MoneyVnd fee = grossAmount.calculatePlatformFee(platformFeePercent);
        MoneyVnd teacherShare = grossAmount.minusOrZero(fee);

        Instant time = occurredAt != null ? occurredAt : Instant.now();
        String periodStr = PERIOD_FORMATTER.format(time);

        return new RevenueSplit(
                UUID.randomUUID(),
                teacherId,
                studentId,
                courseId,
                orderId,
                orderItemId,
                grossAmount,
                fee,
                teacherShare,
                platformFeePercent,
                teacherPct,
                periodStr,
                null,
                time
        );
    }

    public static RevenueSplit calculateStandard(
            UUID teacherId,
            UUID studentId,
            UUID courseId,
            UUID orderId,
            UUID orderItemId,
            MoneyVnd grossAmount,
            Instant occurredAt
    ) {
        return calculate(
                teacherId,
                studentId,
                courseId,
                orderId,
                orderItemId,
                grossAmount,
                DEFAULT_PLATFORM_FEE_PERCENT,
                occurredAt
        );
    }

    public void assignToPayoutPeriod(UUID periodId) {
        if (periodId == null) {
            throw new DomainException("INVALID_PAYOUT_PERIOD", "Payout Period ID không được null");
        }
        this.payoutPeriodId = periodId;
    }

    public UUID getId() {
        return id;
    }

    public UUID getTeacherId() {
        return teacherId;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public UUID getCourseId() {
        return courseId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public UUID getOrderItemId() {
        return orderItemId;
    }

    public MoneyVnd getGrossAmount() {
        return grossAmount;
    }

    public MoneyVnd getPlatformFee() {
        return platformFee;
    }

    public MoneyVnd getTeacherAmount() {
        return teacherAmount;
    }

    public int getPlatformFeePercent() {
        return platformFeePercent;
    }

    public int getTeacherPercent() {
        return teacherPercent;
    }

    public String getPeriod() {
        return period;
    }

    public UUID getPayoutPeriodId() {
        return payoutPeriodId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RevenueSplit that = (RevenueSplit) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
