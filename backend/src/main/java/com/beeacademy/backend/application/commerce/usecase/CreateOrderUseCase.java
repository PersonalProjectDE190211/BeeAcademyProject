package com.beeacademy.backend.application.commerce.usecase;

import com.beeacademy.backend.application.commerce.dto.CreateOrderCommand;
import com.beeacademy.backend.application.commerce.dto.CreateOrderResult;
import com.beeacademy.backend.application.commerce.dto.OrderItemResult;
import com.beeacademy.backend.domain.commerce.Order;
import com.beeacademy.backend.domain.commerce.OrderItem;
import com.beeacademy.backend.domain.commerce.OrderRepositoryPort;
import com.beeacademy.backend.domain.commerce.RewardVoucher;
import com.beeacademy.backend.domain.commerce.RewardVoucherRepositoryPort;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.common.MoneyVnd;
import com.beeacademy.backend.domain.course.Course;
import com.beeacademy.backend.domain.course.CourseRepositoryPort;
import com.beeacademy.backend.domain.course.CourseStatus;
import com.beeacademy.backend.domain.learning.EnrollmentRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class CreateOrderUseCase {

    private final OrderRepositoryPort orderRepository;
    private final CourseRepositoryPort courseRepository;
    private final EnrollmentRepositoryPort enrollmentRepository;
    private final RewardVoucherRepositoryPort voucherRepository;

    public CreateOrderUseCase(
            OrderRepositoryPort orderRepository,
            CourseRepositoryPort courseRepository,
            EnrollmentRepositoryPort enrollmentRepository,
            RewardVoucherRepositoryPort voucherRepository
    ) {
        this.orderRepository = orderRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.voucherRepository = voucherRepository;
    }

    @Transactional
    public CreateOrderResult execute(CreateOrderCommand command) {
        List<OrderItem> items = new ArrayList<>();
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
            sum = sum.plus(course.getPrice());
        }

        MoneyVnd discount = MoneyVnd.ZERO;
        if (command.voucherCode() != null && !command.voucherCode().isBlank()) {
            RewardVoucher voucher = voucherRepository.findByCode(command.voucherCode().trim())
                    .orElseThrow(() -> new DomainException("VOUCHER_NOT_FOUND", "Mã giảm giá không tồn tại: " + command.voucherCode()));

            if (!voucher.isValid(Instant.now())) {
                throw new DomainException("VOUCHER_EXPIRED", "Mã giảm giá đã hết hạn hoặc không khả dụng");
            }

            discount = voucher.calculateDiscount(sum);
        }

        long randomSuffix = ThreadLocalRandom.current().nextLong(100, 999);
        String paymentRef = "BEE" + (System.currentTimeMillis() % 100000000L) + randomSuffix;
        long orderCode = (System.currentTimeMillis() % 100000000L) * 1000 + randomSuffix;

        Order order = Order.create(
                command.studentId(),
                paymentRef,
                command.paymentMethod() != null ? command.paymentMethod() : "PAYOS",
                items,
                discount,
                Order.DEFAULT_EXPIRY_MINUTES
        );

        String paymentLinkId = "PAYOS_" + orderCode;
        order.attachPayOsLink(orderCode, paymentLinkId);

        Order saved = orderRepository.save(order);

        String qrCodeUrl = String.format("https://img.vietqr.io/image/ICB-BEEACADEMY-compact2.png?amount=%d&addInfo=%s",
                saved.getTotalAmount().amount(), saved.getPaymentRef());
        String checkoutUrl = "https://pay.payos.vn/web/" + paymentLinkId;

        List<OrderItemResult> itemResults = saved.getItems().stream()
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

        return new CreateOrderResult(
                saved.getId(),
                saved.getOrderCode(),
                saved.getPaymentRef(),
                checkoutUrl,
                qrCodeUrl,
                saved.getTotalAmount().amount(),
                saved.getDiscountAmount().amount(),
                saved.getStatus().name(),
                saved.getExpiresAt(),
                itemResults
        );
    }
}

