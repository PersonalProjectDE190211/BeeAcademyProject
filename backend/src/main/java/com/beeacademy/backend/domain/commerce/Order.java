package com.beeacademy.backend.domain.commerce;

import com.beeacademy.backend.domain.commerce.event.OrderCreatedDomainEvent;
import com.beeacademy.backend.domain.commerce.event.OrderPaidDomainEvent;
import com.beeacademy.backend.domain.common.AbstractAggregateRoot;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.common.MoneyVnd;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root đại diện cho Đơn hàng mua khóa học (UC09, UC10).
 * Quy tắc nghiệp vụ cốt lõi:
 * - Thời gian thanh toán tối đa 15 phút (expiresAt).
 * - Trạng thái đơn hàng: PENDING -> PAID, CANCELLED, hoặc EXPIRED.
 * - Khi thanh toán thành công (markPaid), phát sinh sự kiện OrderPaidDomainEvent
 *   để tự động kích hoạt Enrollment và phân chia doanh thu (RevenueSplit).
 */
public class Order extends AbstractAggregateRoot {

    public static final int DEFAULT_EXPIRY_MINUTES = 15;

    private final UUID id;
    private final UUID studentId;
    private String paymentRef;
    private Long orderCode;
    private String paymentLinkId;
    private String paymentMethod;
    private OrderStatus status;
    private final List<OrderItem> items = new ArrayList<>();
    private MoneyVnd totalAmount;
    private MoneyVnd discountAmount;
    private final Instant expiresAt;
    private Instant paidAt;
    private final Instant createdAt;
    private Instant updatedAt;

    public Order(
            UUID id,
            UUID studentId,
            String paymentRef,
            Long orderCode,
            String paymentLinkId,
            String paymentMethod,
            OrderStatus status,
            List<OrderItem> items,
            MoneyVnd totalAmount,
            MoneyVnd discountAmount,
            Instant expiresAt,
            Instant paidAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        if (id == null) {
            throw new DomainException("INVALID_ORDER", "Order ID không được để trống");
        }
        if (studentId == null) {
            throw new DomainException("INVALID_ORDER", "Student ID không được để trống");
        }
        if (items == null || items.isEmpty()) {
            throw new DomainException("EMPTY_ORDER_ITEMS", "Đơn hàng phải chứa ít nhất một khóa học");
        }
        this.id = id;
        this.studentId = studentId;
        this.paymentRef = paymentRef;
        this.orderCode = orderCode;
        this.paymentLinkId = paymentLinkId;
        this.paymentMethod = paymentMethod == null ? "PAYOS" : paymentMethod.toUpperCase();
        this.status = status == null ? OrderStatus.PENDING : status;
        this.items.addAll(items);
        this.discountAmount = discountAmount == null ? MoneyVnd.ZERO : discountAmount;
        this.totalAmount = totalAmount != null ? totalAmount : calculateTotalFromItems();
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
        this.expiresAt = expiresAt == null ? this.createdAt.plus(DEFAULT_EXPIRY_MINUTES, ChronoUnit.MINUTES) : expiresAt;
        this.paidAt = paidAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
    }

    public static Order create(
            UUID studentId,
            String paymentRef,
            String paymentMethod,
            List<OrderItem> items,
            MoneyVnd discountAmount,
            int expiryMinutes
    ) {
        UUID orderId = UUID.randomUUID();
        Instant now = Instant.now();
        Instant expires = now.plus(expiryMinutes <= 0 ? DEFAULT_EXPIRY_MINUTES : expiryMinutes, ChronoUnit.MINUTES);

        MoneyVnd discount = discountAmount == null ? MoneyVnd.ZERO : discountAmount;

        // Tính tổng tiền từ danh sách items
        MoneyVnd sum = MoneyVnd.ZERO;
        for (OrderItem item : items) {
            sum = sum.plus(item.getPriceAtPurchase());
        }
        MoneyVnd finalTotal = sum.minusOrZero(discount);

        Order order = new Order(
                orderId,
                studentId,
                paymentRef,
                null,
                null,
                paymentMethod,
                OrderStatus.PENDING,
                items,
                finalTotal,
                discount,
                expires,
                null,
                now,
                now
        );

        order.registerDomainEvent(new OrderCreatedDomainEvent(orderId, studentId, finalTotal, paymentRef));
        return order;
    }

    private MoneyVnd calculateTotalFromItems() {
        MoneyVnd sum = MoneyVnd.ZERO;
        for (OrderItem item : this.items) {
            sum = sum.plus(item.getPriceAtPurchase());
        }
        return sum.minusOrZero(this.discountAmount);
    }

    /**
     * Xác nhận thanh toán thành công (UC10 - PayOS Webhook / SePay Callback).
     */
    public void markPaid(String confirmedPaymentRef, Long assignedOrderCode, Instant paymentTime) {
        markPaid(confirmedPaymentRef, assignedOrderCode, paymentTime, false);
    }

    /**
     * Xác nhận thanh toán thành công.
     * Khi forceAcceptVerifiedPayment = true (dòng tiền thực tế đã được ngân hàng/PayOS xác nhận thành công),
     * cho phép ghi nhận PAID ngay cả khi đơn vừa chạm ngưỡng EXPIRED hoặc CANCELLED do độ trễ mạng (Grace Period).
     */
    public void markPaid(String confirmedPaymentRef, Long assignedOrderCode, Instant paymentTime, boolean forceAcceptVerifiedPayment) {
        if (this.status == OrderStatus.PAID) {
            return; // Idempotent: nếu đã thanh toán thì bỏ qua an toàn
        }
        if (!forceAcceptVerifiedPayment && this.status != OrderStatus.PENDING) {
            throw new DomainException("INVALID_ORDER_STATE",
                    "Đơn hàng đang ở trạng thái '" + this.status + "', không thể chuyển thành đã thanh toán");
        }
        Instant now = paymentTime != null ? paymentTime : Instant.now();
        if (!forceAcceptVerifiedPayment && now.isAfter(this.expiresAt)) {
            this.status = OrderStatus.EXPIRED;
            this.updatedAt = now;
            throw new DomainException("ORDER_EXPIRED", "Đơn hàng đã hết hạn thanh toán (15 phút)");
        }

        if (confirmedPaymentRef != null && !confirmedPaymentRef.isBlank()) {
            this.paymentRef = confirmedPaymentRef;
        }
        if (assignedOrderCode != null) {
            this.orderCode = assignedOrderCode;
        }
        this.status = OrderStatus.PAID;
        this.paidAt = now;
        this.updatedAt = now;
        for (OrderItem item : this.items) {
            item.markPaid();
        }

        registerDomainEvent(new OrderPaidDomainEvent(
                this.id,
                this.studentId,
                this.totalAmount,
                this.paymentRef,
                this.orderCode,
                Collections.unmodifiableList(this.items)
        ));
    }

    /**
     * Gắn mã thanh toán PayOS và liên kết cổng thanh toán.
     */
    public void attachPayOsLink(Long orderCode, String paymentLinkId) {
        if (this.status != OrderStatus.PENDING) {
            throw new DomainException("INVALID_ORDER_STATE", "Không thể gắn cổng thanh toán khi đơn hàng không ở trạng thái PENDING");
        }
        this.orderCode = orderCode;
        this.paymentLinkId = paymentLinkId;
        this.updatedAt = Instant.now();
    }

    /**
     * Hủy đơn hàng trước khi thanh toán.
     */
    public void cancel(String reason) {
        if (this.status == OrderStatus.PAID) {
            throw new DomainException("CANNOT_CANCEL_PAID_ORDER", "Đơn hàng đã thanh toán thành công, không thể hủy");
        }
        this.status = OrderStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }

    /**
     * Đánh dấu hết hạn đơn hàng nếu quá hạn.
     */
    public boolean checkAndMarkExpired(Instant now) {
        Instant checkTime = now != null ? now : Instant.now();
        if (this.status == OrderStatus.PENDING && checkTime.isAfter(this.expiresAt)) {
            this.status = OrderStatus.EXPIRED;
            this.updatedAt = checkTime;
            return true;
        }
        return false;
    }

    public UUID getId() {
        return id;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public String getPaymentRef() {
        return paymentRef;
    }

    public Long getOrderCode() {
        return orderCode;
    }

    public String getPaymentLinkId() {
        return paymentLinkId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public MoneyVnd getTotalAmount() {
        return totalAmount;
    }

    public MoneyVnd getDiscountAmount() {
        return discountAmount;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getPaidAt() {
        return paidAt;
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
        Order order = (Order) o;
        return Objects.equals(id, order.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
