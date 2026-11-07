package com.beeacademy.backend.infrastructure.commerce.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class OrderJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "payment_ref", nullable = false, unique = true, length = 50)
    private String paymentRef;

    @Column(name = "order_code", unique = true)
    private Long orderCode;

    @Column(name = "payment_link_id", length = 100)
    private String paymentLinkId;

    @Column(name = "payment_method", nullable = false, length = 20)
    private String paymentMethod;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "total_amount_vnd", nullable = false)
    private int totalAmountVnd;

    @Column(name = "discount_amount_vnd", nullable = false)
    private int discountAmountVnd;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItemJpaEntity> items = new ArrayList<>();

    public OrderJpaEntity() {
    }

    public OrderJpaEntity(
            UUID id,
            UUID studentId,
            String paymentRef,
            Long orderCode,
            String paymentLinkId,
            String paymentMethod,
            String status,
            int totalAmountVnd,
            int discountAmountVnd,
            Instant expiresAt,
            Instant paidAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.studentId = studentId;
        this.paymentRef = paymentRef;
        this.orderCode = orderCode;
        this.paymentLinkId = paymentLinkId;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.totalAmountVnd = totalAmountVnd;
        this.discountAmountVnd = discountAmountVnd;
        this.expiresAt = expiresAt;
        this.paidAt = paidAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void addItem(OrderItemJpaEntity item) {
        items.add(item);
        item.setOrder(this);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public void setStudentId(UUID studentId) {
        this.studentId = studentId;
    }

    public String getPaymentRef() {
        return paymentRef;
    }

    public void setPaymentRef(String paymentRef) {
        this.paymentRef = paymentRef;
    }

    public Long getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(Long orderCode) {
        this.orderCode = orderCode;
    }

    public String getPaymentLinkId() {
        return paymentLinkId;
    }

    public void setPaymentLinkId(String paymentLinkId) {
        this.paymentLinkId = paymentLinkId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getTotalAmountVnd() {
        return totalAmountVnd;
    }

    public void setTotalAmountVnd(int totalAmountVnd) {
        this.totalAmountVnd = totalAmountVnd;
    }

    public int getDiscountAmountVnd() {
        return discountAmountVnd;
    }

    public void setDiscountAmountVnd(int discountAmountVnd) {
        this.discountAmountVnd = discountAmountVnd;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
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

    public List<OrderItemJpaEntity> getItems() {
        return items;
    }

    public void setItems(List<OrderItemJpaEntity> items) {
        this.items = items;
    }
}

