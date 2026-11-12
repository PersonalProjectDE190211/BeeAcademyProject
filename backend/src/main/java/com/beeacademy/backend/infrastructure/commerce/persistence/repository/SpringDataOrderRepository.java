package com.beeacademy.backend.infrastructure.commerce.persistence.repository;

import com.beeacademy.backend.infrastructure.commerce.persistence.entity.OrderJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataOrderRepository extends JpaRepository<OrderJpaEntity, UUID> {

    Optional<OrderJpaEntity> findByPaymentRef(String paymentRef);

    Optional<OrderJpaEntity> findByOrderCode(Long orderCode);

    List<OrderJpaEntity> findByStudentIdOrderByCreatedAtDesc(UUID studentId);

    List<OrderJpaEntity> findByStatusAndExpiresAtBefore(String status, Instant threshold);

    List<OrderJpaEntity> findByStatusOrderByCreatedAtDesc(String status);
}

