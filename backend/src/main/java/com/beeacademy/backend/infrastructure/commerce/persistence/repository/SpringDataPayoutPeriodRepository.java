package com.beeacademy.backend.infrastructure.commerce.persistence.repository;

import com.beeacademy.backend.infrastructure.commerce.persistence.entity.PayoutPeriodJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataPayoutPeriodRepository extends JpaRepository<PayoutPeriodJpaEntity, UUID> {

    Optional<PayoutPeriodJpaEntity> findByTeacherIdAndMonthYear(UUID teacherId, String monthYear);

    List<PayoutPeriodJpaEntity> findByTeacherIdOrderByMonthYearDesc(UUID teacherId);

    List<PayoutPeriodJpaEntity> findByStatusOrderByMonthYearDesc(String status);
}

