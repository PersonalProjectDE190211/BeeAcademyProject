package com.beeacademy.backend.infrastructure.commerce.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "revenue_splits")
public class RevenueSplitJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "teacher_id", nullable = false)
    private UUID teacherId;

    @Column(name = "student_id")
    private UUID studentId;

    @Column(name = "course_id")
    private UUID courseId;

    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "order_item_id")
    private UUID orderItemId;

    @Column(name = "gross_vnd", nullable = false)
    private int grossVnd;

    @Column(name = "platform_fee_vnd", nullable = false)
    private int platformFeeVnd;

    @Column(name = "teacher_amount_vnd", nullable = false)
    private int teacherAmountVnd;

    @Column(name = "gross_amount", nullable = false)
    private int grossAmount;

    @Column(name = "platform_fee", nullable = false)
    private int platformFee;

    @Column(name = "teacher_amount", nullable = false)
    private int teacherAmount;

    @Column(name = "platform_fee_pct")
    private BigDecimal platformFeePct;

    @Column(name = "teacher_percent")
    private int teacherPercent;

    @Column(name = "period", nullable = false)
    private String period;

    @Column(name = "payout_period_id")
    private UUID payoutPeriodId;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    public RevenueSplitJpaEntity() {
    }

    public RevenueSplitJpaEntity(
            UUID id,
            UUID teacherId,
            UUID studentId,
            UUID courseId,
            UUID orderId,
            UUID orderItemId,
            int grossAmount,
            int platformFee,
            int teacherAmount,
            BigDecimal platformFeePct,
            int teacherPercent,
            String period,
            UUID payoutPeriodId,
            Instant occurredAt
    ) {
        this.id = id;
        this.teacherId = teacherId;
        this.studentId = studentId;
        this.courseId = courseId;
        this.orderId = orderId;
        this.orderItemId = orderItemId;
        this.grossAmount = grossAmount;
        this.grossVnd = grossAmount;
        this.platformFee = platformFee;
        this.platformFeeVnd = platformFee;
        this.teacherAmount = teacherAmount;
        this.teacherAmountVnd = teacherAmount;
        this.platformFeePct = platformFeePct;
        this.teacherPercent = teacherPercent;
        this.period = period;
        this.payoutPeriodId = payoutPeriodId;
        this.occurredAt = occurredAt;
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

    public UUID getStudentId() {
        return studentId;
    }

    public void setStudentId(UUID studentId) {
        this.studentId = studentId;
    }

    public UUID getCourseId() {
        return courseId;
    }

    public void setCourseId(UUID courseId) {
        this.courseId = courseId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public UUID getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(UUID orderItemId) {
        this.orderItemId = orderItemId;
    }

    public int getGrossVnd() {
        return grossVnd;
    }

    public void setGrossVnd(int grossVnd) {
        this.grossVnd = grossVnd;
        this.grossAmount = grossVnd;
    }

    public int getPlatformFeeVnd() {
        return platformFeeVnd;
    }

    public void setPlatformFeeVnd(int platformFeeVnd) {
        this.platformFeeVnd = platformFeeVnd;
        this.platformFee = platformFeeVnd;
    }

    public int getTeacherAmountVnd() {
        return teacherAmountVnd;
    }

    public void setTeacherAmountVnd(int teacherAmountVnd) {
        this.teacherAmountVnd = teacherAmountVnd;
        this.teacherAmount = teacherAmountVnd;
    }

    public int getGrossAmount() {
        return grossAmount;
    }

    public void setGrossAmount(int grossAmount) {
        this.grossAmount = grossAmount;
        this.grossVnd = grossAmount;
    }

    public int getPlatformFee() {
        return platformFee;
    }

    public void setPlatformFee(int platformFee) {
        this.platformFee = platformFee;
        this.platformFeeVnd = platformFee;
    }

    public int getTeacherAmount() {
        return teacherAmount;
    }

    public void setTeacherAmount(int teacherAmount) {
        this.teacherAmount = teacherAmount;
        this.teacherAmountVnd = teacherAmount;
    }

    public BigDecimal getPlatformFeePct() {
        return platformFeePct;
    }

    public void setPlatformFeePct(BigDecimal platformFeePct) {
        this.platformFeePct = platformFeePct;
    }

    public int getTeacherPercent() {
        return teacherPercent;
    }

    public void setTeacherPercent(int teacherPercent) {
        this.teacherPercent = teacherPercent;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public UUID getPayoutPeriodId() {
        return payoutPeriodId;
    }

    public void setPayoutPeriodId(UUID payoutPeriodId) {
        this.payoutPeriodId = payoutPeriodId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }
}

