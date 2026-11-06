package com.beeacademy.backend.domain.commerce;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PayoutPeriodRepositoryPort {

    PayoutPeriod save(PayoutPeriod period);

    Optional<PayoutPeriod> findById(UUID id);

    Optional<PayoutPeriod> findByTeacherIdAndMonthYear(UUID teacherId, String monthYear);

    List<PayoutPeriod> findByTeacherId(UUID teacherId);

    List<PayoutPeriod> findByStatus(PayoutStatus status);
}

