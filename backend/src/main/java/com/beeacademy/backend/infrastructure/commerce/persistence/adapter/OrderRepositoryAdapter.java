package com.beeacademy.backend.infrastructure.commerce.persistence.adapter;

import com.beeacademy.backend.domain.commerce.Order;
import com.beeacademy.backend.domain.commerce.OrderItem;
import com.beeacademy.backend.domain.commerce.OrderItemStatus;
import com.beeacademy.backend.domain.commerce.OrderRepositoryPort;
import com.beeacademy.backend.domain.commerce.OrderStatus;
import com.beeacademy.backend.domain.common.MoneyVnd;
import com.beeacademy.backend.infrastructure.commerce.persistence.entity.OrderJpaEntity;
import com.beeacademy.backend.infrastructure.commerce.persistence.entity.OrderItemJpaEntity;
import com.beeacademy.backend.infrastructure.commerce.persistence.repository.SpringDataOrderItemRepository;
import com.beeacademy.backend.infrastructure.commerce.persistence.repository.SpringDataOrderRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class OrderRepositoryAdapter implements OrderRepositoryPort {

    private final SpringDataOrderRepository orderRepository;
    private final SpringDataOrderItemRepository orderItemRepository;

    public OrderRepositoryAdapter(
            SpringDataOrderRepository orderRepository,
            SpringDataOrderItemRepository orderItemRepository
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Override
    @Transactional
    public Order save(Order order) {
        OrderJpaEntity orderEntity = new OrderJpaEntity(
                order.getId(),
                order.getStudentId(),
                order.getPaymentRef(),
                order.getOrderCode(),
                order.getPaymentLinkId(),
                order.getPaymentMethod(),
                order.getStatus().name().toLowerCase(),
                order.getTotalAmount().amount(),
                order.getDiscountAmount().amount(),
                order.getExpiresAt(),
                order.getPaidAt(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );

        OrderJpaEntity savedOrder = orderRepository.save(orderEntity);

        List<OrderItemJpaEntity> itemEntities = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            OrderItemJpaEntity itemEntity = new OrderItemJpaEntity(
                    item.getId(),
                    savedOrder,
                    item.getCourseId(),
                    item.getTeacherId() != null ? item.getTeacherId() : UUID.randomUUID(),
                    item.getCourseTitle(),
                    item.getPriceAtPurchase().amount(),
                    BigDecimal.valueOf(item.getPlatformFeeRate()),
                    item.getPlatformFee().amount(),
                    item.getTeacherAmount().amount(),
                    item.getItemStatus().name().toLowerCase(),
                    item.getPayoutPeriodId(),
                    order.getCreatedAt(),
                    item.getRefundedAt()
            );
            itemEntities.add(itemEntity);
        }

        orderItemRepository.saveAll(itemEntities);
        return toDomain(savedOrder, itemEntities);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findById(UUID id) {
        return orderRepository.findById(id).map(this::loadWithItems);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findByPaymentRef(String paymentRef) {
        return orderRepository.findByPaymentRef(paymentRef).map(this::loadWithItems);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findByOrderCode(Long orderCode) {
        return orderRepository.findByOrderCode(orderCode).map(this::loadWithItems);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findByStudentId(UUID studentId) {
        return orderRepository.findByStudentIdOrderByCreatedAtDesc(studentId).stream()
                .map(this::loadWithItems)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findPendingOrdersExpiredBefore(Instant threshold) {
        return orderRepository.findByStatusAndExpiresAtBefore("pending", threshold).stream()
                .map(this::loadWithItems)
                .collect(Collectors.toList());
    }

    private Order loadWithItems(OrderJpaEntity entity) {
        List<OrderItemJpaEntity> itemEntities = orderItemRepository.findByOrderId(entity.getId());
        return toDomain(entity, itemEntities);
    }

    private Order toDomain(OrderJpaEntity entity, List<OrderItemJpaEntity> itemEntities) {
        List<OrderItem> items = itemEntities.stream()
                .map(this::toDomainItem)
                .collect(Collectors.toList());

        OrderStatus status = OrderStatus.valueOf(entity.getStatus().toUpperCase());

        return new Order(
                entity.getId(),
                entity.getStudentId(),
                entity.getPaymentRef(),
                entity.getOrderCode(),
                entity.getPaymentLinkId(),
                entity.getPaymentMethod(),
                status,
                items,
                MoneyVnd.of(entity.getTotalAmountVnd()),
                MoneyVnd.of(entity.getDiscountAmountVnd()),
                entity.getExpiresAt(),
                entity.getPaidAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private OrderItem toDomainItem(OrderItemJpaEntity entity) {
        OrderItemStatus itemStatus = OrderItemStatus.valueOf(entity.getItemStatus().toUpperCase());
        int feeRate = entity.getPlatformFeeRate() != null ? entity.getPlatformFeeRate().intValue() : 20;

        return new OrderItem(
                entity.getId(),
                entity.getOrder() != null ? entity.getOrder().getId() : null,
                entity.getCourseId(),
                entity.getTeacherId(),
                entity.getCourseTitleSnapshot(),
                MoneyVnd.of(entity.getPriceAtPurchase()),
                feeRate,
                MoneyVnd.of(entity.getPlatformFeeVnd()),
                MoneyVnd.of(entity.getTeacherAmountVnd()),
                itemStatus,
                entity.getPayoutPeriodId(),
                entity.getRefundedAt()
        );
    }
}

