package com.beeacademy.backend.infrastructure.commerce.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "order_items")
public class OrderItemJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderJpaEntity order;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(name = "teacher_id", nullable = false)
    private UUID teacherId;

    @Column(name = "course_title_snapshot", nullable = false)
    private String courseTitleSnapshot;

    @Column(name = "price_at_purchase", nullable = false)
    private int priceAtPurchase;

    @Column(name = "platform_fee_rate", nullable = false)
    private BigDecimal platformFeeRate;

    @Column(name = "platform_fee_vnd", nullable = false)
    private int platformFeeVnd;

    @Column(name = "teacher_amount_vnd", nullable = false)
    private int teacherAmountVnd;

    @Column(name = "item_status", nullable = false, length = 20)
    private String itemStatus;

    @Column(name = "payout_period_id")
    private UUID payoutPeriodId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "refunded_at")
    private Instant refundedAt;

    public OrderItemJpaEntity() {
    }

    public OrderItemJpaEntity(
            UUID id,
            OrderJpaEntity order,
            UUID courseId,
            UUID teacherId,
            String courseTitleSnapshot,
            int priceAtPurchase,
            BigDecimal platformFeeRate,
            int platformFeeVnd,
            int teacherAmountVnd,
            String itemStatus,
            UUID payoutPeriodId,
            Instant createdAt,
            Instant refundedAt
    ) {
        this.id = id;
        this.order = order;
        this.courseId = courseId;
        this.teacherId = teacherId;
        this.courseTitleSnapshot = courseTitleSnapshot;
        this.priceAtPurchase = priceAtPurchase;
        this.platformFeeRate = platformFeeRate;
        this.platformFeeVnd = platformFeeVnd;
        this.teacherAmountVnd = teacherAmountVnd;
        this.itemStatus = itemStatus;
        this.payoutPeriodId = payoutPeriodId;
        this.createdAt = createdAt;
        this.refundedAt = refundedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public OrderJpaEntity getOrder() {
        return order;
    }

    public void setOrder(OrderJpaEntity order) {
        this.order = order;
    }

    public UUID getCourseId() {
        return courseId;
    }

    public void setCourseId(UUID courseId) {
        this.courseId = courseId;
    }

    public UUID getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(UUID teacherId) {
        this.teacherId = teacherId;
    }

    public String getCourseTitleSnapshot() {
        return courseTitleSnapshot;
    }

    public void setCourseTitleSnapshot(String courseTitleSnapshot) {
        this.courseTitleSnapshot = courseTitleSnapshot;
    }

    public int getPriceAtPurchase() {
        return priceAtPurchase;
    }

    public void setPriceAtPurchase(int priceAtPurchase) {
        this.priceAtPurchase = priceAtPurchase;
    }

    public BigDecimal getPlatformFeeRate() {
        return platformFeeRate;
    }

    public void setPlatformFeeRate(BigDecimal platformFeeRate) {
        this.platformFeeRate = platformFeeRate;
    }

    public int getPlatformFeeVnd() {
        return platformFeeVnd;
    }

    public void setPlatformFeeVnd(int platformFeeVnd) {
        this.platformFeeVnd = platformFeeVnd;
    }

    public int getTeacherAmountVnd() {
        return teacherAmountVnd;
    }

    public void setTeacherAmountVnd(int teacherAmountVnd) {
        this.teacherAmountVnd = teacherAmountVnd;
    }

    public String getItemStatus() {
        return itemStatus;
    }

    public void setItemStatus(String itemStatus) {
        this.itemStatus = itemStatus;
    }

    public UUID getPayoutPeriodId() {
        return payoutPeriodId;
    }

    public void setPayoutPeriodId(UUID payoutPeriodId) {
        this.payoutPeriodId = payoutPeriodId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getRefundedAt() {
        return refundedAt;
    }

    public void setRefundedAt(Instant refundedAt) {
        this.refundedAt = refundedAt;
    }
}

