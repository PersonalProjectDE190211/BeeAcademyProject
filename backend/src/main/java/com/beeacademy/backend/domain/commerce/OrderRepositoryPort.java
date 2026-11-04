package com.beeacademy.backend.domain.commerce;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepositoryPort {

    Order save(Order order);

    Optional<Order> findById(UUID id);

    Optional<Order> findByPaymentRef(String paymentRef);

    Optional<Order> findByOrderCode(Long orderCode);

    List<Order> findByStudentId(UUID studentId);

    List<Order> findPendingOrdersExpiredBefore(Instant threshold);
}
