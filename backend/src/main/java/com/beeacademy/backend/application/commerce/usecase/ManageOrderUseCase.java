package com.beeacademy.backend.application.commerce.usecase;

import com.beeacademy.backend.application.commerce.dto.OrderItemResult;
import com.beeacademy.backend.application.commerce.dto.OrderResult;
import com.beeacademy.backend.domain.commerce.Order;
import com.beeacademy.backend.domain.commerce.OrderRepositoryPort;
import com.beeacademy.backend.domain.commerce.OrderStatus;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.infrastructure.common.adapter.PayOsGatewayAdapter;
import com.beeacademy.backend.infrastructure.common.adapter.PayOsGatewayAdapter.PayOsLinkStatus;
import com.beeacademy.backend.infrastructure.common.adapter.PayOsGatewayAdapter.PayOsPaymentLinkInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ManageOrderUseCase {

    private static final Logger log = LoggerFactory.getLogger(ManageOrderUseCase.class);

    private final OrderRepositoryPort orderRepository;
    private final PayOsGatewayAdapter payOsGatewayAdapter;
    private final ProcessPaymentWebhookUseCase processPaymentWebhookUseCase;

    public ManageOrderUseCase(OrderRepositoryPort orderRepository) {
        this(orderRepository, null, null);
    }

    @Autowired
    public ManageOrderUseCase(
            OrderRepositoryPort orderRepository,
            PayOsGatewayAdapter payOsGatewayAdapter,
            ProcessPaymentWebhookUseCase processPaymentWebhookUseCase
    ) {
        this.orderRepository = orderRepository;
        this.payOsGatewayAdapter = payOsGatewayAdapter;
        this.processPaymentWebhookUseCase = processPaymentWebhookUseCase;
    }

    @Transactional
    public List<OrderResult> getStudentOrders(UUID studentId) {
        List<Order> rawOrders = orderRepository.findByStudentId(studentId);
        List<OrderResult> results = new ArrayList<>();
        Instant now = Instant.now();

        for (Order order : rawOrders) {
            Order current = synchronizePendingOrderWithGateway(order, now);
            results.add(toOrderResult(current));
        }
        return results;
    }

    @Transactional
    public OrderResult getOrderById(UUID orderId, UUID studentId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new DomainException("ORDER_NOT_FOUND", "Không tìm thấy đơn hàng với ID: " + orderId));

        if (!order.getStudentId().equals(studentId)) {
            throw new DomainException("FORBIDDEN", "Bạn không có quyền xem đơn hàng này");
        }

        Order synced = synchronizePendingOrderWithGateway(order, Instant.now());
        return toOrderResult(synced);
    }

    /**
     * Chủ động đối chiếu trạng thái thanh toán của đơn hàng trực tiếp với cổng PayOS.
     * Nếu PayOS báo đã thanh toán (PAID) -> tự động kích hoạt khóa học và chia doanh thu 80/20 ngay lập tức.
     */
    @Transactional
    public OrderResult verifyOrderPayment(UUID orderId, UUID studentId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new DomainException("ORDER_NOT_FOUND", "Không tìm thấy đơn hàng với ID: " + orderId));

        if (!order.getStudentId().equals(studentId)) {
            throw new DomainException("FORBIDDEN", "Bạn không có quyền kiểm tra đơn hàng này");
        }

        if (order.getStatus() == OrderStatus.PAID) {
            return toOrderResult(order);
        }

        Order synced = synchronizePendingOrderWithGateway(order, Instant.now());
        return toOrderResult(synced);
    }

    /**
     * Hủy đơn hàng đang chờ thanh toán.
     * Ngoại lệ quan trọng: Trước khi hủy phải hỏi PayOS xem khách có vừa mới quét QR chuyển tiền xong không:
     * - Nếu PayOS báo đã PAID -> Không hủy, mà kích hoạt khóa học cho học sinh luôn!
     * - Nếu chưa PAID -> Gọi PayOS khóa mã QR ngay lập tức rồi mới đổi trạng thái DB sang CANCELLED.
     */
    @Transactional
    public OrderResult cancelOrder(UUID orderId, UUID studentId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new DomainException("ORDER_NOT_FOUND", "Không tìm thấy đơn hàng với ID: " + orderId));

        if (!order.getStudentId().equals(studentId)) {
            throw new DomainException("FORBIDDEN", "Bạn không có quyền hủy đơn hàng này");
        }

        // Kiểm tra xem khách đã lỡ chuyển tiền trên PayOS ngay trước khi bấm nút Hủy hay chưa
        if (order.getStatus() == OrderStatus.PENDING && payOsGatewayAdapter != null && order.getOrderCode() != null) {
            PayOsLinkStatus liveStatus = payOsGatewayAdapter.queryPaymentStatus(order.getOrderCode());
            if (liveStatus.paid() && processPaymentWebhookUseCase != null) {
                log.info("Phát hiện đơn {} (code={}) đã được thanh toán trên PayOS ngay khi học sinh bấm Hủy. Tự động kích hoạt khóa học thay vì hủy!",
                        order.getId(), order.getOrderCode());
                processPaymentWebhookUseCase.fulfillPaidOrder(order, liveStatus.reference(), order.getOrderCode(), true);
                Order paidOrder = orderRepository.findById(orderId).orElse(order);
                return toOrderResult(paidOrder);
            }
        }

        order.cancel("Học sinh yêu cầu hủy đơn hàng");
        if (payOsGatewayAdapter != null && order.getOrderCode() != null) {
            payOsGatewayAdapter.cancelPaymentLinkSafely(order.getOrderCode(), "Student cancelled order");
        }

        Order saved = orderRepository.save(order);
        return toOrderResult(saved);
    }

    /**
     * Đồng bộ một đơn hàng đang PENDING với trạng thái thực tế trên cổng PayOS.
     */
    public Order synchronizePendingOrderWithGateway(Order order, Instant now) {
        if (order.getStatus() != OrderStatus.PENDING) {
            return order;
        }

        if (payOsGatewayAdapter != null && order.getOrderCode() != null) {
            PayOsLinkStatus liveStatus = payOsGatewayAdapter.queryPaymentStatus(order.getOrderCode());
            if (liveStatus.paid() && processPaymentWebhookUseCase != null) {
                log.info("Đối soát chủ động với PayOS phát hiện đơn {} (code={}) đã PAID. Tiến hành kích hoạt khóa học!",
                        order.getId(), order.getOrderCode());
                processPaymentWebhookUseCase.fulfillPaidOrder(order, liveStatus.reference(), order.getOrderCode(), true);
                return orderRepository.findById(order.getId()).orElse(order);
            }
            if ("CANCELLED".equalsIgnoreCase(liveStatus.status())) {
                order.cancel("Đơn hàng đã bị hủy trên cổng PayOS");
                return orderRepository.save(order);
            }
        }

        if (order.checkAndMarkExpired(now)) {
            if (payOsGatewayAdapter != null && order.getOrderCode() != null) {
                payOsGatewayAdapter.cancelPaymentLinkSafely(order.getOrderCode(), "Order expired after 15 minutes");
            }
            return orderRepository.save(order);
        }

        return order;
    }

    private OrderResult toOrderResult(Order order) {
        List<OrderItemResult> items = order.getItems().stream()
                .map(i -> new OrderItemResult(
                        i.getId(),
                        i.getCourseId(),
                        i.getTeacherId(),
                        i.getCourseTitle(),
                        i.getPriceAtPurchase().amount(),
                        i.getPlatformFeeRate(),
                        i.getPlatformFee().amount(),
                        i.getTeacherAmount().amount(),
                        i.getItemStatus().name()
                ))
                .collect(Collectors.toList());

        PayOsPaymentLinkInfo linkInfo = null;
        if (order.getStatus() == OrderStatus.PENDING && payOsGatewayAdapter != null && order.getOrderCode() != null) {
            linkInfo = payOsGatewayAdapter.getOrRebuildLinkInfo(
                    order.getOrderCode(),
                    order.getTotalAmount().amount(),
                    order.getPaymentRef(),
                    order.getPaymentLinkId()
            );
        }

        return new OrderResult(
                order.getId(),
                order.getStudentId(),
                order.getOrderCode(),
                order.getPaymentRef(),
                order.getPaymentMethod(),
                order.getStatus().name(),
                order.getTotalAmount().amount(),
                order.getDiscountAmount().amount(),
                order.getExpiresAt(),
                order.getPaidAt(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                items,
                linkInfo != null ? linkInfo.checkoutUrl() : null,
                linkInfo != null ? linkInfo.qrCodeUrl() : null,
                linkInfo != null ? linkInfo.bankBin() : "970422",
                linkInfo != null ? linkInfo.bankName() : "Ngân hàng Quân Đội (MBBank)",
                linkInfo != null ? linkInfo.accountNumber() : "0348756972",
                linkInfo != null ? linkInfo.accountName() : "BEE ACADEMY"
        );
    }
}
