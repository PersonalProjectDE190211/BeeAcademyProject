package com.beeacademy.backend.application.commerce.usecase;

import com.beeacademy.backend.application.commerce.dto.CreateOrderCommand;
import com.beeacademy.backend.application.commerce.dto.CreateOrderResult;
import com.beeacademy.backend.application.commerce.dto.OrderItemResult;
import com.beeacademy.backend.domain.commerce.Order;
import com.beeacademy.backend.domain.commerce.OrderItem;
import com.beeacademy.backend.domain.commerce.OrderRepositoryPort;
import com.beeacademy.backend.domain.commerce.OrderStatus;
import com.beeacademy.backend.domain.commerce.RewardVoucher;
import com.beeacademy.backend.domain.commerce.RewardVoucherRepositoryPort;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.common.MoneyVnd;
import com.beeacademy.backend.domain.course.Course;
import com.beeacademy.backend.domain.course.CourseRepositoryPort;
import com.beeacademy.backend.domain.course.CourseStatus;
import com.beeacademy.backend.domain.learning.Enrollment;
import com.beeacademy.backend.domain.learning.EnrollmentRepositoryPort;
import com.beeacademy.backend.infrastructure.common.adapter.PayOsGatewayAdapter;
import com.beeacademy.backend.infrastructure.common.adapter.PayOsGatewayAdapter.PayOsItem;
import com.beeacademy.backend.infrastructure.common.adapter.PayOsGatewayAdapter.PayOsPaymentLinkInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Service
public class CreateOrderUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateOrderUseCase.class);

    private final OrderRepositoryPort orderRepository;
    private final CourseRepositoryPort courseRepository;
    private final EnrollmentRepositoryPort enrollmentRepository;
    private final RewardVoucherRepositoryPort voucherRepository;
    private final PayOsGatewayAdapter payOsGatewayAdapter;

    // Khóa theo Student ID để chống Double-Click / Tạo 2 đơn hàng cùng lúc trong 1 mili-giây
    private final ConcurrentHashMap<UUID, ReentrantLock> studentOrderLocks = new ConcurrentHashMap<>();
    // Khóa toàn cục cho Voucher để chống Race Condition khi 2 người cùng giành 1 mã giảm giá cuối cùng
    private final Object voucherConcurrencyLock = new Object();

    public CreateOrderUseCase(
            OrderRepositoryPort orderRepository,
            CourseRepositoryPort courseRepository,
            EnrollmentRepositoryPort enrollmentRepository,
            RewardVoucherRepositoryPort voucherRepository
    ) {
        this(orderRepository, courseRepository, enrollmentRepository, voucherRepository, null);
    }

    @Autowired
    public CreateOrderUseCase(
            OrderRepositoryPort orderRepository,
            CourseRepositoryPort courseRepository,
            EnrollmentRepositoryPort enrollmentRepository,
            RewardVoucherRepositoryPort voucherRepository,
            PayOsGatewayAdapter payOsGatewayAdapter
    ) {
        this.orderRepository = orderRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.voucherRepository = voucherRepository;
        this.payOsGatewayAdapter = payOsGatewayAdapter;
    }

    @Transactional(rollbackFor = Exception.class)
    public CreateOrderResult execute(CreateOrderCommand command) {
        ReentrantLock studentLock = studentOrderLocks.computeIfAbsent(command.studentId(), k -> new ReentrantLock());
        studentLock.lock();
        try {
            return doCreateOrder(command);
        } finally {
            studentLock.unlock();
            studentOrderLocks.remove(command.studentId(), studentLock);
        }
    }

    private CreateOrderResult doCreateOrder(CreateOrderCommand command) {
        Instant now = Instant.now();
        Set<UUID> requestedCourseIds = new HashSet<>(command.courseIds());

        // 1. Kiểm tra các đơn PENDING hiện có của học sinh:
        //    - Nếu đã có đơn PENDING chứa đúng các khóa học này và còn hạn > 2 phút (và không đổi voucher) -> Tái sử dụng ngay (Chống Double-Click)
        //    - Nếu có đơn PENDING cũ đã hết hạn hoặc bị trùng khóa học -> Tự động hủy đơn cũ trên cả DB và PayOS để tránh học sinh quét nhầm QR cũ.
        List<Order> existingOrders = orderRepository.findByStudentId(command.studentId());
        if (existingOrders != null) {
            for (Order existing : existingOrders) {
                if (existing.getStatus() != OrderStatus.PENDING) {
                    continue;
                }
                if (existing.checkAndMarkExpired(now)) {
                    if (payOsGatewayAdapter != null && existing.getOrderCode() != null) {
                        payOsGatewayAdapter.cancelPaymentLinkSafely(existing.getOrderCode(), "Order expired");
                    }
                    orderRepository.save(existing);
                    continue;
                }

                Set<UUID> existingCourseIds = existing.getItems().stream()
                        .map(OrderItem::getCourseId)
                        .collect(Collectors.toSet());

                boolean hasVoucherRequest = command.voucherCode() != null && !command.voucherCode().isBlank();
                if (!hasVoucherRequest
                        && existingCourseIds.equals(requestedCourseIds)
                        && existing.getExpiresAt().isAfter(now.plus(2, ChronoUnit.MINUTES))
                        && existing.getOrderCode() != null) {
                    log.info("Tái sử dụng đơn hàng PENDING còn hạn (code={}) cho học sinh {} để chống tạo đơn trùng.",
                            existing.getOrderCode(), command.studentId());
                    return buildResultFromExistingOrder(existing);
                }

                // Nếu đơn PENDING cũ có chứa bất kỳ khóa học nào đang chuẩn bị mua mới -> Hủy đơn cũ và khóa QR cũ trên PayOS
                boolean overlaps = existingCourseIds.stream().anyMatch(requestedCourseIds::contains);
                if (overlaps) {
                    existing.cancel("Thay thế bằng đơn hàng mới");
                    if (payOsGatewayAdapter != null && existing.getOrderCode() != null) {
                        payOsGatewayAdapter.cancelPaymentLinkSafely(existing.getOrderCode(), "Replaced by newer order");
                    }
                    orderRepository.save(existing);
                }
            }
        }

        // 2. Kiểm tra khóa học và chụp giá (Price Snapshot) tại thời điểm tạo đơn
        List<OrderItem> items = new ArrayList<>();
        List<PayOsItem> payOsItems = new ArrayList<>();
        MoneyVnd sum = MoneyVnd.ZERO;

        for (UUID courseId : command.courseIds()) {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new DomainException("COURSE_NOT_FOUND", "Không tìm thấy khóa học với ID: " + courseId));

            if (course.getStatus() != CourseStatus.PUBLISHED) {
                throw new DomainException("COURSE_NOT_PUBLISHED", "Khóa học '" + course.getTitle() + "' chưa được mở bán");
            }

            if (enrollmentRepository.existsByStudentIdAndCourseId(command.studentId(), courseId)) {
                throw new DomainException("ALREADY_ENROLLED", "Học sinh đã đăng ký khóa học '" + course.getTitle() + "'");
            }

            OrderItem item = OrderItem.createWithTeacher(
                    UUID.randomUUID(),
                    course.getId(),
                    course.getTeacherId(),
                    course.getTitle(),
                    course.getPrice(),
                    OrderItem.DEFAULT_PLATFORM_FEE_RATE
            );
            items.add(item);
            payOsItems.add(new PayOsItem(course.getTitle(), 1, course.getPrice().amount()));
            sum = sum.plus(course.getPrice());
        }

        // 3. Xử lý mã giảm giá (Khóa đồng bộ chống tranh chấp suất cuối khi 2 người cùng áp 1 mã Voucher cùng lúc)
        MoneyVnd discount = MoneyVnd.ZERO;
        if (command.voucherCode() != null && !command.voucherCode().isBlank()) {
            synchronized (voucherConcurrencyLock) {
                RewardVoucher voucher = voucherRepository.findByCode(command.voucherCode().trim())
                        .orElseThrow(() -> new DomainException("VOUCHER_NOT_FOUND", "Mã giảm giá không tồn tại: " + command.voucherCode()));

                if (!voucher.isValid(now)) {
                    throw new DomainException("VOUCHER_EXPIRED", "Mã giảm giá đã hết lượt sử dụng hoặc đã hết hạn");
                }

                discount = voucher.calculateDiscount(sum);
                voucher.use(command.studentId(), now);
                voucherRepository.save(voucher);
            }
        }

        // Sinh orderCode và paymentRef ngắn gọn <= 25 ký tự theo chuẩn PayOS/Napas
        long randomSuffix = ThreadLocalRandom.current().nextLong(100, 999);
        long orderCode = (System.currentTimeMillis() % 100000000L) * 1000 + randomSuffix;
        String paymentRef = "BEE" + orderCode;

        Order order = Order.create(
                command.studentId(),
                paymentRef,
                command.paymentMethod() != null ? command.paymentMethod() : "PAYOS",
                items,
                discount,
                Order.DEFAULT_EXPIRY_MINUTES
        );

        // 4. Ngoại lệ Đơn hàng 0đ (Voucher giảm 100%): Kích hoạt thẳng PAID và mở khóa học, không đẩy sang PayOS
        if (order.getTotalAmount().isZero()) {
            order.attachPayOsLink(orderCode, "FREE_VOUCHER_" + orderCode);
            order.markPaid(paymentRef, orderCode, now, true);
            Order savedZeroOrder = orderRepository.save(order);
            for (OrderItem item : savedZeroOrder.getItems()) {
                if (!enrollmentRepository.existsByStudentIdAndCourseId(command.studentId(), item.getCourseId())) {
                    enrollmentRepository.save(Enrollment.create(command.studentId(), item.getCourseId(), savedZeroOrder.getId()));
                }
            }
            return new CreateOrderResult(
                    savedZeroOrder.getId(),
                    savedZeroOrder.getOrderCode(),
                    savedZeroOrder.getPaymentRef(),
                    null,
                    null,
                    0,
                    savedZeroOrder.getDiscountAmount().amount(),
                    savedZeroOrder.getStatus().name(),
                    savedZeroOrder.getExpiresAt(),
                    toItemResults(savedZeroOrder),
                    null,
                    null,
                    null,
                    null
            );
        }

        // 5. Tạo link thanh toán QR động trên PayOS & Bù trừ (Compensating Rollback) nếu lưu DB thất bại
        PayOsPaymentLinkInfo linkInfo = null;
        if (payOsGatewayAdapter != null) {
            linkInfo = payOsGatewayAdapter.createPaymentLink(
                    orderCode,
                    order.getTotalAmount().amount(),
                    paymentRef,
                    payOsItems,
                    order.getExpiresAt()
            );
        }

        String paymentLinkId = linkInfo != null ? linkInfo.paymentLinkId() : ("PAYOS_" + orderCode);
        order.attachPayOsLink(orderCode, paymentLinkId);

        Order saved;
        try {
            saved = orderRepository.save(order);
        } catch (Exception dbEx) {
            // Cơ chế Rollback bù trừ: Nếu lưu DB thất bại sau khi đã tạo link trên PayOS -> Hủy ngay link trên PayOS!
            if (payOsGatewayAdapter != null && linkInfo != null && linkInfo.livePayOsLink()) {
                log.error("Lỗi lưu đơn hàng vào DB sau khi đã tạo link PayOS (code={}). Thực hiện Compensating Cancel trên PayOS!", orderCode);
                payOsGatewayAdapter.cancelPaymentLinkSafely(orderCode, "Database save rollback");
            }
            throw dbEx;
        }

        String qrCodeUrl = linkInfo != null
                ? linkInfo.qrCodeUrl()
                : String.format("https://img.vietqr.io/image/970422-0348756972-compact2.png?amount=%d&addInfo=%s&accountName=BEE%%20ACADEMY",
                saved.getTotalAmount().amount(), saved.getPaymentRef());
        String checkoutUrl = linkInfo != null ? linkInfo.checkoutUrl() : ("https://pay.payos.vn/web/" + paymentLinkId);

        return new CreateOrderResult(
                saved.getId(),
                saved.getOrderCode(),
                linkInfo != null ? linkInfo.transferDescription() : saved.getPaymentRef(),
                checkoutUrl,
                qrCodeUrl,
                saved.getTotalAmount().amount(),
                saved.getDiscountAmount().amount(),
                saved.getStatus().name(),
                saved.getExpiresAt(),
                toItemResults(saved),
                linkInfo != null ? linkInfo.bankBin() : "970422",
                linkInfo != null ? linkInfo.bankName() : "Ngân hàng Quân Đội (MBBank)",
                linkInfo != null ? linkInfo.accountNumber() : "0348756972",
                linkInfo != null ? linkInfo.accountName() : "BEE ACADEMY"
        );
    }

    private CreateOrderResult buildResultFromExistingOrder(Order existing) {
        PayOsPaymentLinkInfo linkInfo = payOsGatewayAdapter != null
                ? payOsGatewayAdapter.getOrRebuildLinkInfo(
                existing.getOrderCode(),
                existing.getTotalAmount().amount(),
                existing.getPaymentRef(),
                existing.getPaymentLinkId()
        )
                : null;

        String qrCodeUrl = linkInfo != null
                ? linkInfo.qrCodeUrl()
                : String.format("https://img.vietqr.io/image/970422-0348756972-compact2.png?amount=%d&addInfo=%s&accountName=BEE%%20ACADEMY",
                existing.getTotalAmount().amount(), existing.getPaymentRef());
        String checkoutUrl = linkInfo != null
                ? linkInfo.checkoutUrl()
                : ("https://pay.payos.vn/web/" + existing.getPaymentLinkId());

        return new CreateOrderResult(
                existing.getId(),
                existing.getOrderCode(),
                linkInfo != null ? linkInfo.transferDescription() : existing.getPaymentRef(),
                checkoutUrl,
                qrCodeUrl,
                existing.getTotalAmount().amount(),
                existing.getDiscountAmount().amount(),
                existing.getStatus().name(),
                existing.getExpiresAt(),
                toItemResults(existing),
                linkInfo != null ? linkInfo.bankBin() : "970422",
                linkInfo != null ? linkInfo.bankName() : "Ngân hàng Quân Đội (MBBank)",
                linkInfo != null ? linkInfo.accountNumber() : "0348756972",
                linkInfo != null ? linkInfo.accountName() : "BEE ACADEMY"
        );
    }

    private List<OrderItemResult> toItemResults(Order order) {
        return order.getItems().stream()
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
    }
}
