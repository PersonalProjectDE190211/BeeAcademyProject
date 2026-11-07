package com.beeacademy.backend.infrastructure.commerce.persistence.repository;

import com.beeacademy.backend.infrastructure.commerce.persistence.entity.OrderItemJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataOrderItemRepository extends JpaRepository<OrderItemJpaEntity, UUID> {

    List<OrderItemJpaEntity> findByOrderId(UUID orderId);

    List<OrderItemJpaEntity> findByTeacherIdAndItemStatus(UUID teacherId, String itemStatus);
}

