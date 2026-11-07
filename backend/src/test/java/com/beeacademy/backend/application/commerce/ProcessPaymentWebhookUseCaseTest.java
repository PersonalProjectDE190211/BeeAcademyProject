package com.beeacademy.backend.application.commerce;

import com.beeacademy.backend.application.commerce.dto.PayOsWebhookPayload;
import com.beeacademy.backend.application.commerce.dto.WebhookProcessResult;
import com.beeacademy.backend.application.commerce.usecase.ProcessPaymentWebhookUseCase;
import com.beeacademy.backend.domain.commerce.Order;
import com.beeacademy.backend.domain.commerce.OrderItem;
import com.beeacademy.backend.domain.commerce.OrderRepositoryPort;
import com.beeacademy.backend.domain.commerce.OrderStatus;
import com.beeacademy.backend.domain.commerce.PayoutPeriod;
import com.beeacademy.backend.domain.commerce.PayoutPeriodRepositoryPort;
import com.beeacademy.backend.domain.commerce.RevenueSplit;
import com.beeacademy.backend.domain.commerce.RevenueSplitRepositoryPort;
import com.beeacademy.backend.domain.common.MoneyVnd;
import com.beeacademy.backend.domain.learning.Enrollment;
import com.beeacademy.backend.domain.learning.EnrollmentRepositoryPort;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessPaymentWebhookUseCaseTest {

    @Mock
    private OrderRepositoryPort orderRepository;

    @Mock
    private EnrollmentRepositoryPort enrollmentRepository;

    @Mock
    private RevenueSplitRepositoryPort revenueSplitRepository;

    @Mock
    private PayoutPeriodRepositoryPort payoutPeriodRepository;

    private ProcessPaymentWebhookUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ProcessPaymentWebhookUseCase(
                orderRepository,
                enrollmentRepository,
                revenueSplitRepository,
                payoutPeriodRepository
        );
    }

    @Test
    @DisplayName("Xử lý Webhook PayOS thành công: chuyển PAID, kích hoạt Enrollment, ghi sổ cái 80/20")
    void processWebhook_Success() {
        UUID studentId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        long orderCode = 88991122L;
        String paymentRef = "BEE88991122";

        OrderItem item = OrderItem.createWithTeacher(
                UUID.randomUUID(),
                courseId,
                teacherId,
                "Khóa học Hóa học 11",
                MoneyVnd.of(500000),
                20
        );

        Order order = Order.create(
                studentId,
                paymentRef,
                "PAYOS",
                List.of(item),
                MoneyVnd.ZERO,
                15
        );
        order.attachPayOsLink(orderCode, "PAYOS_" + orderCode);

        when(orderRepository.findByOrderCode(orderCode)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
        when(enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)).thenReturn(false);
        when(revenueSplitRepository.save(any(RevenueSplit.class))).thenAnswer(i -> i.getArgument(0));
        when(payoutPeriodRepository.findByTeacherIdAndMonthYear(any(), any())).thenReturn(Optional.empty());
        when(payoutPeriodRepository.save(any(PayoutPeriod.class))).thenAnswer(i -> i.getArgument(0));

        PayOsWebhookPayload payload = new PayOsWebhookPayload(
                "00",
                "success",
                true,
                new PayOsWebhookPayload.PayOsWebhookData(orderCode, 500000, paymentRef, "123", "FT999", "2026-05-01", "VND", "PLINK", "00", "success"),
                "signature"
        );

        WebhookProcessResult result = useCase.execute(payload);

        assertThat(result.success()).isTrue();
        assertThat(result.orderStatus()).isEqualTo("PAID");

        verify(orderRepository).save(any(Order.class));
        verify(enrollmentRepository).save(any(Enrollment.class));
        verify(revenueSplitRepository).save(any(RevenueSplit.class));
        verify(payoutPeriodRepository).save(any(PayoutPeriod.class));
    }

    @Test
    @DisplayName("Tính bất biến Idempotent: Webhook gọi lần 2 khi đơn đã PAID thì trả về thành công ngay")
    void processWebhook_Idempotent_AlreadyPaid() {
        UUID studentId = UUID.randomUUID();
        long orderCode = 77112233L;
        String paymentRef = "BEE77112233";

        OrderItem item = OrderItem.createWithTeacher(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Khóa học Lý 12",
                MoneyVnd.of(400000),
                20
        );

        Order order = Order.create(studentId, paymentRef, "PAYOS", List.of(item), MoneyVnd.ZERO, 15);
        order.attachPayOsLink(orderCode, "PAYOS_" + orderCode);
        order.markPaid(paymentRef, orderCode, null); // Đã thanh toán trước đó

        when(orderRepository.findByOrderCode(orderCode)).thenReturn(Optional.of(order));

        PayOsWebhookPayload payload = new PayOsWebhookPayload(
                "00",
                "success",
                true,
                new PayOsWebhookPayload.PayOsWebhookData(orderCode, 400000, paymentRef, "123", "FT999", "2026-05-01", "VND", "PLINK", "00", "success"),
                "signature"
        );

        WebhookProcessResult result = useCase.execute(payload);

        assertThat(result.success()).isTrue();
        assertThat(result.orderStatus()).isEqualTo("PAID");
        assertThat(result.message()).contains("Idempotent");

        verify(orderRepository, never()).save(any(Order.class));
        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }
}

