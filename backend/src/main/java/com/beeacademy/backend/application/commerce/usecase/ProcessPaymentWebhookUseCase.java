package com.beeacademy.backend.application.commerce.usecase;

import com.beeacademy.backend.application.commerce.dto.PayOsWebhookPayload;
import com.beeacademy.backend.application.commerce.dto.WebhookProcessResult;
import com.beeacademy.backend.domain.commerce.Order;
import com.beeacademy.backend.domain.commerce.OrderItem;
import com.beeacademy.backend.domain.commerce.OrderRepositoryPort;
import com.beeacademy.backend.domain.commerce.OrderStatus;
import com.beeacademy.backend.domain.commerce.PayoutPeriod;
import com.beeacademy.backend.domain.commerce.PayoutPeriodRepositoryPort;
import com.beeacademy.backend.domain.commerce.RevenueSplit;
import com.beeacademy.backend.domain.commerce.RevenueSplitRepositoryPort;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.learning.Enrollment;
import com.beeacademy.backend.domain.learning.EnrollmentRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class ProcessPaymentWebhookUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcessPaymentWebhookUseCase.class);

    private final OrderRepositoryPort orderRepository;
    private final EnrollmentRepositoryPort enrollmentRepository;
    private final RevenueSplitRepositoryPort revenueSplitRepository;
    private final PayoutPeriodRepositoryPort payoutPeriodRepository;

    public ProcessPaymentWebhookUseCase(
            OrderRepositoryPort orderRepository,
            EnrollmentRepositoryPort enrollmentRepository,
            RevenueSplitRepositoryPort revenueSplitRepository,
            PayoutPeriodRepositoryPort payoutPeriodRepository
    ) {
        this.orderRepository = orderRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.revenueSplitRepository = revenueSplitRepository;
        this.payoutPeriodRepository = payoutPeriodRepository;
    }

    @Transactional
    public WebhookProcessResult execute(PayOsWebhookPayload payload) {
        if (payload == null) {
            throw new DomainException("INVALID_WEBHOOK_PAYLOAD", "Payload Webhook không được null");
        }

        Long orderCode = null;
        String reference = null;
        String description = null;

        if (payload.data() != null) {
            orderCode = payload.data().orderCode();
            reference = payload.data().reference();
            description = payload.data().description();
        }

        Optional<Order> orderOpt = Optional.empty();
        if (orderCode != null) {
            orderOpt = orderRepository.findByOrderCode(orderCode);
        }
        if (orderOpt.isEmpty() && reference != null) {
            orderOpt = orderRepository.findByPaymentRef(reference);
        }
        if (orderOpt.isEmpty() && description != null) {
            orderOpt = orderRepository.findByPaymentRef(description.trim());
        }

        if (orderOpt.isEmpty()) {
            log.warn("Không tìm thấy đơn hàng cho webhook PayOS: orderCode={}, reference={}", orderCode, reference);
            return new WebhookProcessResult(false, null, orderCode, "NOT_FOUND", "Không tìm thấy đơn hàng khớp với webhook");
        }

        Order order = orderOpt.get();

        // Idempotency: Nếu đơn đã thanh toán thì trả về thành công an toàn ngay lập tức
        if (order.getStatus() == OrderStatus.PAID) {
            log.info("Đơn hàng {} (code={}) đã ở trạng thái PAID. Bỏ qua xử lý lặp lại (Idempotent).", order.getId(), order.getOrderCode());
            return new WebhookProcessResult(true, order.getId(), order.getOrderCode(), "PAID", "Đơn hàng đã được thanh toán trước đó (Idempotent)");
        }

        // Kiểm tra mã kết quả từ PayOS ("00" nghĩa là giao dịch thành công)
        boolean isSuccess = "00".equals(payload.code()) || Boolean.TRUE.equals(payload.success());
        if (payload.data() != null && "00".equals(payload.data().code())) {
            isSuccess = true;
        }

        if (!isSuccess) {
            log.warn("Giao dịch PayOS không thành công cho đơn {}: code={}, desc={}", order.getId(), payload.code(), payload.desc());
            return new WebhookProcessResult(false, order.getId(), order.getOrderCode(), order.getStatus().name(), "Giao dịch thanh toán không thành công");
        }

        // 1. Chuyển trạng thái đơn hàng sang PAID
        Instant now = Instant.now();
        order.markPaid(reference != null ? reference : order.getPaymentRef(), orderCode, now);
        Order savedOrder = orderRepository.save(order);
        log.info("Đơn hàng {} đã chuyển trạng thái sang PAID lúc {}", savedOrder.getId(), savedOrder.getPaidAt());

        // 2. Kích hoạt Enrollment cho Học sinh đối với từng khóa học trong đơn hàng
        for (OrderItem item : savedOrder.getItems()) {
            boolean alreadyEnrolled = enrollmentRepository.existsByStudentIdAndCourseId(savedOrder.getStudentId(), item.getCourseId());
            if (!alreadyEnrolled) {
                Enrollment enrollment = Enrollment.create(savedOrder.getStudentId(), item.getCourseId(), savedOrder.getId());
                enrollmentRepository.save(enrollment);
                log.info("Đã kích hoạt ghi danh cho học sinh {} vào khóa học {}", savedOrder.getStudentId(), item.getCourseId());
            }

            // 3. Phân chia doanh thu kế toán kép (Double-Entry Ledger) & Cập nhật PayoutPeriod
            if (item.getTeacherId() != null) {
                RevenueSplit split = RevenueSplit.calculate(
                        item.getTeacherId(),
                        savedOrder.getStudentId(),
                        item.getCourseId(),
                        savedOrder.getId(),
                        item.getId(),
                        item.getPriceAtPurchase(),
                        item.getPlatformFeeRate(),
                        now
                );

                // Tìm hoặc khởi tạo kỳ thanh toán đối soát của tháng
                PayoutPeriod period = payoutPeriodRepository.findByTeacherIdAndMonthYear(item.getTeacherId(), split.getPeriod())
                        .orElseGet(() -> PayoutPeriod.create(item.getTeacherId(), split.getPeriod()));

                period.addSplit(split);
                PayoutPeriod savedPeriod = payoutPeriodRepository.save(period);

                split.assignToPayoutPeriod(savedPeriod.getId());
                revenueSplitRepository.save(split);
                log.info("Đã ghi nhận doanh thu giáo viên {}: Gross={}, Fee={}, TeacherShare={}",
                        item.getTeacherId(), split.getGrossAmount(), split.getPlatformFee(), split.getTeacherAmount());
            }
        }

        return new WebhookProcessResult(
                true,
                savedOrder.getId(),
                savedOrder.getOrderCode(),
                "PAID",
                "Xác nhận thanh toán và kích hoạt khóa học thành công"
        );
    }
}

