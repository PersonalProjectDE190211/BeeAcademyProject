package com.beeacademy.backend.infrastructure.commerce.persistence.repository;

import com.beeacademy.backend.infrastructure.commerce.persistence.entity.RevenueSplitJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataRevenueSplitRepository extends JpaRepository<RevenueSplitJpaEntity, UUID> {

    List<RevenueSplitJpaEntity> findByTeacherIdOrderByOccurredAtDesc(UUID teacherId);

    List<RevenueSplitJpaEntity> findByTeacherIdAndPeriodOrderByOccurredAtDesc(UUID teacherId, String period);

    List<RevenueSplitJpaEntity> findByOrderId(UUID orderId);

    List<RevenueSplitJpaEntity> findByTeacherIdAndPeriodAndPayoutPeriodIdIsNullOrderByOccurredAtDesc(UUID teacherId, String period);
}

