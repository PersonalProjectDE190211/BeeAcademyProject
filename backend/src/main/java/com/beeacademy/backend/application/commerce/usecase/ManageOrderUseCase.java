package com.beeacademy.backend.application.commerce.usecase;

import com.beeacademy.backend.application.commerce.dto.OrderItemResult;
import com.beeacademy.backend.application.commerce.dto.OrderResult;
import com.beeacademy.backend.domain.commerce.Order;
import com.beeacademy.backend.domain.commerce.OrderRepositoryPort;
import com.beeacademy.backend.domain.common.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ManageOrderUseCase {

    private final OrderRepositoryPort orderRepository;

    public ManageOrderUseCase(OrderRepositoryPort orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public List<OrderResult> getStudentOrders(UUID studentId) {
        return orderRepository.findByStudentId(studentId).stream()
                .map(this::toOrderResult)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderResult getOrderById(UUID orderId, UUID studentId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new DomainException("ORDER_NOT_FOUND", "Không tìm thấy đơn hàng với ID: " + orderId));

        if (!order.getStudentId().equals(studentId)) {
            throw new DomainException("FORBIDDEN", "Bạn không có quyền xem đơn hàng này");
        }

        return toOrderResult(order);
    }

    @Transactional
    public OrderResult cancelOrder(UUID orderId, UUID studentId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new DomainException("ORDER_NOT_FOUND", "Không tìm thấy đơn hàng với ID: " + orderId));

        if (!order.getStudentId().equals(studentId)) {
            throw new DomainException("FORBIDDEN", "Bạn không có quyền hủy đơn hàng này");
        }

        order.cancel("Học sinh yêu cầu hủy đơn hàng");
        Order saved = orderRepository.save(order);
        return toOrderResult(saved);
    }

    private OrderResult toOrderResult(Order order) {
        List<OrderItemResult> items = order.getItems().stream()
                .map(i -> new OrderItemResult(
                        i.getId(),
                        i.getCourseId(),
                        i.getTeacherId(),
                        i.getCourseTitle(),
                        i.getPriceAtPurchase().amount(),
                        i.getPlatformFeeRate(),
                        i.getPlatformFee().amount(),
                        i.getTeacherAmount().amount(),
                        i.getItemStatus().name()
                ))
                .collect(Collectors.toList());

        return new OrderResult(
                order.getId(),
                order.getStudentId(),
                order.getOrderCode(),
                order.getPaymentRef(),
                order.getPaymentMethod(),
                order.getStatus().name(),
                order.getTotalAmount().amount(),
                order.getDiscountAmount().amount(),
                order.getExpiresAt(),
                order.getPaidAt(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                items
        );
    }
}

