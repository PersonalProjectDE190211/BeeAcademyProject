package com.beeacademy.backend.infrastructure.commerce.persistence.adapter;

import com.beeacademy.backend.domain.commerce.RevenueSplit;
import com.beeacademy.backend.domain.commerce.RevenueSplitRepositoryPort;
import com.beeacademy.backend.domain.common.MoneyVnd;
import com.beeacademy.backend.infrastructure.commerce.persistence.entity.RevenueSplitJpaEntity;
import com.beeacademy.backend.infrastructure.commerce.persistence.repository.SpringDataRevenueSplitRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class RevenueSplitRepositoryAdapter implements RevenueSplitRepositoryPort {

    private final SpringDataRevenueSplitRepository revenueSplitRepository;

    public RevenueSplitRepositoryAdapter(SpringDataRevenueSplitRepository revenueSplitRepository) {
        this.revenueSplitRepository = revenueSplitRepository;
    }

    @Override
    @Transactional
    public RevenueSplit save(RevenueSplit split) {
        RevenueSplitJpaEntity entity = new RevenueSplitJpaEntity(
                split.getId(),
                split.getTeacherId(),
                split.getStudentId(),
                split.getCourseId(),
                split.getOrderId(),
                split.getOrderItemId(),
                split.getGrossAmount().amount(),
                split.getPlatformFee().amount(),
                split.getTeacherAmount().amount(),
                BigDecimal.valueOf(split.getPlatformFeePercent()),
                split.getTeacherPercent(),
                split.getPeriod(),
                split.getPayoutPeriodId(),
                split.getOccurredAt()
        );

        RevenueSplitJpaEntity saved = revenueSplitRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RevenueSplit> findById(UUID id) {
        return revenueSplitRepository.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RevenueSplit> findByTeacherId(UUID teacherId) {
        return revenueSplitRepository.findByTeacherIdOrderByOccurredAtDesc(teacherId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RevenueSplit> findByTeacherIdAndPeriod(UUID teacherId, String period) {
        return revenueSplitRepository.findByTeacherIdAndPeriodOrderByOccurredAtDesc(teacherId, period).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RevenueSplit> findByOrderId(UUID orderId) {
        return revenueSplitRepository.findByOrderId(orderId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RevenueSplit> findUnassignedByTeacherIdAndPeriod(UUID teacherId, String period) {
        return revenueSplitRepository.findByTeacherIdAndPeriodAndPayoutPeriodIdIsNullOrderByOccurredAtDesc(teacherId, period).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private RevenueSplit toDomain(RevenueSplitJpaEntity entity) {
        int feePct = entity.getPlatformFeePct() != null
                ? entity.getPlatformFeePct().intValue()
                : (100 - entity.getTeacherPercent());

        int teacherPct = entity.getTeacherPercent() > 0
                ? entity.getTeacherPercent()
                : (100 - feePct);

        return new RevenueSplit(
                entity.getId(),
                entity.getTeacherId(),
                entity.getStudentId(),
                entity.getCourseId(),
                entity.getOrderId(),
                entity.getOrderItemId(),
                MoneyVnd.of(entity.getGrossAmount() > 0 ? entity.getGrossAmount() : entity.getGrossVnd()),
                MoneyVnd.of(entity.getPlatformFee() > 0 ? entity.getPlatformFee() : entity.getPlatformFeeVnd()),
                MoneyVnd.of(entity.getTeacherAmount() > 0 ? entity.getTeacherAmount() : entity.getTeacherAmountVnd()),
                feePct,
                teacherPct,
                entity.getPeriod(),
                entity.getPayoutPeriodId(),
                entity.getOccurredAt()
        );
    }
}

