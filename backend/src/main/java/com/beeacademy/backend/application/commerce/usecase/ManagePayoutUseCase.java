package com.beeacademy.backend.application.commerce.usecase;

import com.beeacademy.backend.application.commerce.dto.ConfirmPayoutCommand;
import com.beeacademy.backend.application.commerce.dto.PayoutPeriodResult;
import com.beeacademy.backend.application.commerce.dto.TeacherRevenueStatsResult;
import com.beeacademy.backend.domain.commerce.PayoutPeriod;
import com.beeacademy.backend.domain.commerce.PayoutPeriodRepositoryPort;
import com.beeacademy.backend.domain.commerce.PayoutStatus;
import com.beeacademy.backend.domain.common.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ManagePayoutUseCase {

    private static final DateTimeFormatter PERIOD_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM")
            .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));

    private final PayoutPeriodRepositoryPort payoutPeriodRepository;

    public ManagePayoutUseCase(PayoutPeriodRepositoryPort payoutPeriodRepository) {
        this.payoutPeriodRepository = payoutPeriodRepository;
    }

    @Transactional(readOnly = true)
    public List<PayoutPeriodResult> getTeacherPayouts(UUID teacherId) {
        return payoutPeriodRepository.findByTeacherId(teacherId).stream()
                .map(this::toResult)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TeacherRevenueStatsResult getTeacherRevenueStatistics(UUID teacherId) {
        List<PayoutPeriod> periods = payoutPeriodRepository.findByTeacherId(teacherId);

        int totalGross = 0;
        int totalFee = 0;
        int totalTeacher = 0;
        int pendingPayout = 0;
        int paidPayout = 0;
        int totalTransactions = 0;

        for (PayoutPeriod p : periods) {
            totalGross += p.getTotalGross().amount();
            totalFee += p.getTotalPlatformFee().amount();
            totalTeacher += p.getTotalTeacherAmount().amount();
            totalTransactions += p.getTransactionCount();

            if (p.getStatus() == PayoutStatus.PAID) {
                paidPayout += p.getTotalTeacherAmount().amount();
            } else if (p.getStatus() == PayoutStatus.PENDING || p.getStatus() == PayoutStatus.PROCESSING) {
                pendingPayout += p.getTotalTeacherAmount().amount();
            }
        }

        String currentPeriod = PERIOD_FORMATTER.format(Instant.now());

        return new TeacherRevenueStatsResult(
                totalGross,
                totalFee,
                totalTeacher,
                pendingPayout,
                paidPayout,
                totalTransactions,
                currentPeriod
        );
    }

    @Transactional(readOnly = true)
    public List<PayoutPeriodResult> getAllPayouts(PayoutStatus status) {
        List<PayoutPeriod> periods = status != null
                ? payoutPeriodRepository.findByStatus(status)
                : payoutPeriodRepository.findByStatus(PayoutStatus.PENDING);

        return periods.stream()
                .map(this::toResult)
                .collect(Collectors.toList());
    }

    @Transactional
    public PayoutPeriodResult confirmPayout(ConfirmPayoutCommand command) {
        PayoutPeriod period = payoutPeriodRepository.findById(command.payoutPeriodId())
                .orElseThrow(() -> new DomainException("PAYOUT_NOT_FOUND", "Không tìm thấy kỳ quyết toán với ID: " + command.payoutPeriodId()));

        period.confirmPayout(
                command.adminId(),
                command.transferRef(),
                command.transferContent(),
                command.uncAttachmentUrl(),
                Instant.now()
        );

        PayoutPeriod saved = payoutPeriodRepository.save(period);
        return toResult(saved);
    }

    private PayoutPeriodResult toResult(PayoutPeriod p) {
        return new PayoutPeriodResult(
                p.getId(),
                p.getTeacherId(),
                p.getMonthYear(),
                p.getStatus().name(),
                p.getTotalGross().amount(),
                p.getTotalPlatformFee().amount(),
                p.getTotalTeacherAmount().amount(),
                p.getTransactionCount(),
                p.getPaidAt(),
                p.getPaidByAdmin(),
                p.getTransferRef(),
                p.getTransferContent(),
                p.getUncAttachmentUrl(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}

