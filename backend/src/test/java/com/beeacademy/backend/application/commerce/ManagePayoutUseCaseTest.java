package com.beeacademy.backend.application.commerce;

import com.beeacademy.backend.application.commerce.dto.ConfirmPayoutCommand;
import com.beeacademy.backend.application.commerce.dto.PayoutPeriodResult;
import com.beeacademy.backend.application.commerce.dto.TeacherRevenueStatsResult;
import com.beeacademy.backend.application.commerce.usecase.ManagePayoutUseCase;
import com.beeacademy.backend.domain.commerce.OrderItem;
import com.beeacademy.backend.domain.commerce.PayoutPeriod;
import com.beeacademy.backend.domain.commerce.PayoutPeriodRepositoryPort;
import com.beeacademy.backend.domain.commerce.PayoutStatus;
import com.beeacademy.backend.domain.commerce.RevenueSplit;
import com.beeacademy.backend.domain.common.MoneyVnd;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManagePayoutUseCaseTest {

    @Mock
    private PayoutPeriodRepositoryPort payoutPeriodRepository;

    private ManagePayoutUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ManagePayoutUseCase(payoutPeriodRepository);
    }

    @Test
    @DisplayName("Admin xác nhận quyết toán thành công chuyển sang PAID kèm mã chuyển khoản UNC")
    void confirmPayout_Success() {
        UUID teacherId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        UUID payoutPeriodId = UUID.randomUUID();

        PayoutPeriod period = PayoutPeriod.create(teacherId, "2026-05");
        RevenueSplit split = RevenueSplit.calculateStandard(
                teacherId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                MoneyVnd.of(1000000),
                null
        );
        period.addSplit(split);

        when(payoutPeriodRepository.findById(payoutPeriodId)).thenReturn(Optional.of(period));
        when(payoutPeriodRepository.save(any(PayoutPeriod.class))).thenAnswer(i -> i.getArgument(0));

        ConfirmPayoutCommand command = new ConfirmPayoutCommand(
                adminId,
                payoutPeriodId,
                "FT20260531_001",
                "Chuyển khoản doanh thu tháng 5",
                "https://res.cloudinary.com/hohjixsz/image/upload/unc_proof.pdf"
        );

        PayoutPeriodResult result = useCase.confirmPayout(command);

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo("PAID");
        assertThat(result.transferRef()).isEqualTo("FT20260531_001");
        assertThat(result.uncAttachmentUrl()).isNotBlank();
        assertThat(period.getStatus()).isEqualTo(PayoutStatus.PAID);

        verify(payoutPeriodRepository).save(period);
    }

    @Test
    @DisplayName("Thống kê doanh thu giáo viên tính toán chính xác tổng doanh thu và tiền chờ quyết toán")
    void getTeacherRevenueStatistics_Success() {
        UUID teacherId = UUID.randomUUID();
        PayoutPeriod period = PayoutPeriod.create(teacherId, "2026-05");
        RevenueSplit split = RevenueSplit.calculateStandard(
                teacherId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                MoneyVnd.of(500000),
                null
        );
        period.addSplit(split);

        when(payoutPeriodRepository.findByTeacherId(teacherId)).thenReturn(List.of(period));

        TeacherRevenueStatsResult stats = useCase.getTeacherRevenueStatistics(teacherId);

        assertThat(stats.totalGrossVnd()).isEqualTo(500000);
        assertThat(stats.totalPlatformFeeVnd()).isEqualTo(100000); // 20%
        assertThat(stats.totalTeacherVnd()).isEqualTo(400000); // 80%
        assertThat(stats.pendingPayoutVnd()).isEqualTo(400000);
        assertThat(stats.totalTransactions()).isEqualTo(1);
    }
}

