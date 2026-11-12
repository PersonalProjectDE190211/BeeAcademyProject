package com.beeacademy.backend.infrastructure.commerce;

import com.beeacademy.backend.application.commerce.usecase.ManageOrderUseCase;
import com.beeacademy.backend.domain.commerce.Order;
import com.beeacademy.backend.domain.commerce.OrderRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * Tiến trình Tự phục hồi & Đối soát Thanh toán ngầm (Disaster Recovery & Self-Healing Reconciliation).
 * Giải quyết triệt để trường hợp:
 * - Khách đã quét mã QR chuyển khoản xong nhưng đúng lúc đó Website/Backend bị sập (Downtime) khiến Webhook bị lỡ.
 * - Khi Backend khởi động lại (@EventListener(ApplicationReadyEvent)) và định kỳ mỗi 60 giây (@Scheduled),
 *   hệ thống tự động quét các đơn hàng đang PENDING để hỏi trực tiếp cổng PayOS:
 *   + Nếu PayOS xác nhận PAID -> Tự động kích hoạt khóa học (Enrollment) và ghi sổ cái doanh thu 80/20.
 *   + Nếu đã quá hạn 15 phút mà chưa thanh toán -> Tự động chuyển sang EXPIRED và hủy mã QR trên PayOS.
 */
@Component
public class PaymentReconciliationScheduler {

    private static final Logger log = LoggerFactory.getLogger(PaymentReconciliationScheduler.class);

    private final OrderRepositoryPort orderRepository;
    private final ManageOrderUseCase manageOrderUseCase;

    public PaymentReconciliationScheduler(
            OrderRepositoryPort orderRepository,
            ManageOrderUseCase manageOrderUseCase
    ) {
        this.orderRepository = orderRepository;
        this.manageOrderUseCase = manageOrderUseCase;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void reconcileOnStartup() {
        log.info("Khởi chạy đối soát tự động các đơn hàng PayOS đang chờ (Startup Reconciliation)...");
        runReconciliationCycle();
    }

    @Scheduled(fixedDelayString = "${app.payment.reconcile-interval-ms:60000}", initialDelayString = "60000")
    public void reconcilePeriodically() {
        runReconciliationCycle();
    }

    public void runReconciliationCycle() {
        try {
            List<Order> pendingOrders = orderRepository.findActivePendingOrders();
            if (pendingOrders == null || pendingOrders.isEmpty()) {
                return;
            }
            Instant now = Instant.now();
            for (Order order : pendingOrders) {
                try {
                    manageOrderUseCase.synchronizePendingOrderWithGateway(order, now);
                } catch (Exception itemEx) {
                    log.debug("Bỏ qua lỗi đối soát đơn lẻ {}: {}", order.getId(), itemEx.getMessage());
                }
            }
        } catch (Exception e) {
            log.debug("Chu kỳ đối soát thanh toán tạm hoãn: {}", e.getMessage());
        }
    }
}

