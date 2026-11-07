package com.beeacademy.backend.infrastructure.commerce.persistence.adapter;

import com.beeacademy.backend.domain.commerce.PayoutPeriod;
import com.beeacademy.backend.domain.commerce.PayoutPeriodRepositoryPort;
import com.beeacademy.backend.domain.commerce.PayoutStatus;
import com.beeacademy.backend.domain.common.MoneyVnd;
import com.beeacademy.backend.infrastructure.commerce.persistence.entity.PayoutPeriodJpaEntity;
import com.beeacademy.backend.infrastructure.commerce.persistence.repository.SpringDataPayoutPeriodRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class PayoutPeriodRepositoryAdapter implements PayoutPeriodRepositoryPort {

    private final SpringDataPayoutPeriodRepository payoutPeriodRepository;

    public PayoutPeriodRepositoryAdapter(SpringDataPayoutPeriodRepository payoutPeriodRepository) {
        this.payoutPeriodRepository = payoutPeriodRepository;
    }

    @Override
    @Transactional
    public PayoutPeriod save(PayoutPeriod period) {
        PayoutPeriodJpaEntity entity = new PayoutPeriodJpaEntity(
                period.getId(),
                period.getTeacherId(),
                period.getMonthYear(),
                period.getStatus().name().toLowerCase(),
                period.getTotalGross().amount(),
                period.getTotalPlatformFee().amount(),
                period.getTotalTeacherAmount().amount(),
                period.getTransactionCount(),
                period.getPaidByAdmin(),
                period.getTransferRef(),
                period.getTransferContent(),
                period.getUncAttachmentUrl(),
                period.getPaidAt(),
                period.getCreatedAt(),
                period.getUpdatedAt()
        );

        PayoutPeriodJpaEntity saved = payoutPeriodRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PayoutPeriod> findById(UUID id) {
        return payoutPeriodRepository.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PayoutPeriod> findByTeacherIdAndMonthYear(UUID teacherId, String monthYear) {
        return payoutPeriodRepository.findByTeacherIdAndMonthYear(teacherId, monthYear).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayoutPeriod> findByTeacherId(UUID teacherId) {
        return payoutPeriodRepository.findByTeacherIdOrderByMonthYearDesc(teacherId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayoutPeriod> findByStatus(PayoutStatus status) {
        return payoutPeriodRepository.findByStatusOrderByMonthYearDesc(status.name().toLowerCase()).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private PayoutPeriod toDomain(PayoutPeriodJpaEntity entity) {
        PayoutStatus status = PayoutStatus.valueOf(entity.getStatus().toUpperCase());

        return new PayoutPeriod(
                entity.getId(),
                entity.getTeacherId(),
                entity.getMonthYear(),
                status,
                MoneyVnd.of(entity.getTotalGrossVnd()),
                MoneyVnd.of(entity.getTotalPlatformFeeVnd()),
                MoneyVnd.of(entity.getTotalTeacherVnd()),
                entity.getTransactionCount(),
                entity.getPaidAt(),
                entity.getPaidByAdmin(),
                entity.getTransferRef(),
                entity.getTransferContent(),
                entity.getUncAttachmentUrl(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}

