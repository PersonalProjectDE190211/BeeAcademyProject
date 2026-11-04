package com.beeacademy.backend.domain.commerce;

import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.common.MoneyVnd;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity đại diện cho một mục khóa học trong đơn hàng mua.
 */
public class OrderItem implements Serializable {

    private final UUID id;
    private final UUID orderId;
    private final UUID courseId;
    private final String courseTitle;
    private final MoneyVnd priceAtPurchase;

    public OrderItem(UUID id, UUID orderId, UUID courseId, String courseTitle, MoneyVnd priceAtPurchase) {
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
        this.id = id;
        this.orderId = orderId;
        this.courseId = courseId;
        this.courseTitle = courseTitle == null ? "" : courseTitle.trim();
        this.priceAtPurchase = priceAtPurchase;
    }

    public static OrderItem create(UUID orderId, UUID courseId, String courseTitle, MoneyVnd priceAtPurchase) {
        return new OrderItem(UUID.randomUUID(), orderId, courseId, courseTitle, priceAtPurchase);
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

    public String getCourseTitle() {
        return courseTitle;
    }

    public MoneyVnd getPriceAtPurchase() {
        return priceAtPurchase;
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
