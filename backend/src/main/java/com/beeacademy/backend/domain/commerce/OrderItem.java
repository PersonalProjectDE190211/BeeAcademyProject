package com.beeacademy.backend.domain.commerce;

import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.common.MoneyVnd;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity đại diện cho một mục khóa học trong đơn hàng mua, tích hợp trực tiếp
 * thông tin phân chia doanh thu (80% Giáo viên / 20% Nền tảng) tại thời điểm mua
 * và trạng thái hoàn tiền (itemStatus: PENDING / PAID / REFUNDED).
 */
public class OrderItem implements Serializable {

    public static final int DEFAULT_PLATFORM_FEE_RATE = 20;

    private final UUID id;
    private final UUID orderId;
    private final UUID courseId;
    private final UUID teacherId;
    private final String courseTitle;
    private final MoneyVnd priceAtPurchase;
    private final int platformFeeRate;
    private final MoneyVnd platformFee;
    private final MoneyVnd teacherAmount;
    private OrderItemStatus itemStatus;
    private UUID payoutPeriodId;
    private Instant refundedAt;

    public OrderItem(
            UUID id,
            UUID orderId,
            UUID courseId,
            UUID teacherId,
            String courseTitle,
            MoneyVnd priceAtPurchase,
            int platformFeeRate,
            MoneyVnd platformFee,
            MoneyVnd teacherAmount,
            OrderItemStatus itemStatus,
            UUID payoutPeriodId,
            Instant refundedAt
    ) {
        if (id == null) {
            throw new DomainException("INVALID_ORDER_ITEM", "ID mục đơn hàng không được để trống");
        }
        if (orderId == null) {
            throw new DomainException("INVALID_ORDER_ITEM", "Order ID không được để trống");
        }
        if (courseId == null) {
            throw new DomainException("INVALID_ORDER_ITEM", "Course ID không được để trống");
        }
        if (priceAtPurchase == null) {
            throw new DomainException("INVALID_ORDER_ITEM", "Giá mua khóa học không được để trống");
        }
        if (platformFeeRate < 0 || platformFeeRate > 100) {
            throw new DomainException("INVALID_FEE_RATE", "Tỷ lệ phí sàn phải từ 0% đến 100%");
        }
        MoneyVnd fee = platformFee != null ? platformFee : priceAtPurchase.calculatePlatformFee(platformFeeRate);
        MoneyVnd teacherShare = teacherAmount != null ? teacherAmount : priceAtPurchase.minusOrZero(fee);

        if (priceAtPurchase.amount() != fee.amount() + teacherShare.amount()) {
            throw new DomainException("ORDER_ITEM_LEDGER_MISMATCH",
                    "Tổng giá mua phải bằng phí nền tảng cộng doanh thu giáo viên");
        }

        this.id = id;
        this.orderId = orderId;
        this.courseId = courseId;
        this.teacherId = teacherId;
        this.courseTitle = courseTitle == null ? "" : courseTitle.trim();
        this.priceAtPurchase = priceAtPurchase;
        this.platformFeeRate = platformFeeRate;
        this.platformFee = fee;
        this.teacherAmount = teacherShare;
        this.itemStatus = itemStatus == null ? OrderItemStatus.PENDING : itemStatus;
        this.payoutPeriodId = payoutPeriodId;
        this.refundedAt = refundedAt;
    }

    public OrderItem(UUID id, UUID orderId, UUID courseId, String courseTitle, MoneyVnd priceAtPurchase) {
        this(
                id,
                orderId,
                courseId,
                null,
                courseTitle,
                priceAtPurchase,
                DEFAULT_PLATFORM_FEE_RATE,
                priceAtPurchase != null ? priceAtPurchase.calculatePlatformFee(DEFAULT_PLATFORM_FEE_RATE) : null,
                priceAtPurchase != null ? priceAtPurchase.calculateTeacherShare(DEFAULT_PLATFORM_FEE_RATE) : null,
                OrderItemStatus.PENDING,
                null,
                null
        );
    }

    public static OrderItem create(UUID orderId, UUID courseId, String courseTitle, MoneyVnd priceAtPurchase) {
        return new OrderItem(UUID.randomUUID(), orderId, courseId, courseTitle, priceAtPurchase);
    }

    public static OrderItem createWithTeacher(
            UUID orderId,
            UUID courseId,
            UUID teacherId,
            String courseTitle,
            MoneyVnd priceAtPurchase,
            int platformFeeRate
    ) {
        MoneyVnd fee = priceAtPurchase.calculatePlatformFee(platformFeeRate);
        MoneyVnd teacherShare = priceAtPurchase.minusOrZero(fee);
        return new OrderItem(
                UUID.randomUUID(),
                orderId,
                courseId,
                teacherId,
                courseTitle,
                priceAtPurchase,
                platformFeeRate,
                fee,
                teacherShare,
                OrderItemStatus.PENDING,
                null,
                null
        );
    }

    public void markPaid() {
        this.itemStatus = OrderItemStatus.PAID;
    }

    public void markRefunded(Instant refundTime) {
        if (this.payoutPeriodId != null) {
            throw new DomainException("ITEM_ALREADY_SETTLED",
                    "Mục đơn hàng đã được quyết toán cho giáo viên, không thể hoàn tiền trực tiếp");
        }
        this.itemStatus = OrderItemStatus.REFUNDED;
        this.refundedAt = refundTime != null ? refundTime : Instant.now();
    }

    public void assignToPayoutPeriod(UUID periodId) {
        if (this.itemStatus != OrderItemStatus.PAID) {
            throw new DomainException("ITEM_NOT_ELIGIBLE_FOR_PAYOUT",
                    "Chỉ mục đơn hàng ở trạng thái PAID mới được đưa vào kỳ quyết toán");
        }
        this.payoutPeriodId = periodId;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public UUID getCourseId() {
        return courseId;
    }

    public UUID getTeacherId() {
        return teacherId;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public MoneyVnd getPriceAtPurchase() {
        return priceAtPurchase;
    }

    public int getPlatformFeeRate() {
        return platformFeeRate;
    }

    public MoneyVnd getPlatformFee() {
        return platformFee;
    }

    public MoneyVnd getTeacherAmount() {
        return teacherAmount;
    }

    public OrderItemStatus getItemStatus() {
        return itemStatus;
    }

    public UUID getPayoutPeriodId() {
        return payoutPeriodId;
    }

    public Instant getRefundedAt() {
        return refundedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        return Objects.equals(id, orderItem.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
