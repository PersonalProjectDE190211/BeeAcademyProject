package com.beeacademy.backend.domain.commerce;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RevenueSplitRepositoryPort {

    RevenueSplit save(RevenueSplit split);

    Optional<RevenueSplit> findById(UUID id);

    List<RevenueSplit> findByTeacherId(UUID teacherId);

    List<RevenueSplit> findByTeacherIdAndPeriod(UUID teacherId, String period);

    List<RevenueSplit> findByOrderId(UUID orderId);

    List<RevenueSplit> findUnassignedByTeacherIdAndPeriod(UUID teacherId, String period);
}

