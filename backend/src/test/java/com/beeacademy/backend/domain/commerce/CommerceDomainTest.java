package com.beeacademy.backend.domain.commerce;

import com.beeacademy.backend.domain.commerce.event.OrderCreatedDomainEvent;
import com.beeacademy.backend.domain.commerce.event.OrderPaidDomainEvent;
import com.beeacademy.backend.domain.commerce.event.PayoutConfirmedDomainEvent;
import com.beeacademy.backend.domain.commerce.event.RewardAwardedDomainEvent;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.common.MoneyVnd;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommerceDomainTest {

    @Nested
    @DisplayName("1. Kiểm thử Đơn hàng (Order Aggregate)")
    class OrderTests {

        @Test
        @DisplayName("Tạo đơn hàng tính đúng tổng tiền và phát sinh OrderCreatedDomainEvent")
        void testCreateOrderSuccess() {
            UUID studentId = UUID.randomUUID();
            UUID courseId = UUID.randomUUID();
            List<OrderItem> items = List.of(
                    OrderItem.create(UUID.randomUUID(), courseId, "Toán 12 Nâng Cao", new MoneyVnd(500_000))
            );

            Order order = Order.create(studentId, "ORD-12345", "PAYOS", items, MoneyVnd.ZERO, 15);

            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(order.getTotalAmount()).isEqualTo(new MoneyVnd(500_000));
            assertThat(order.getExpiresAt()).isAfter(Instant.now());
            assertThat(order.getDomainEvents()).hasSize(1);
            assertThat(order.getDomainEvents().getFirst()).isInstanceOf(OrderCreatedDomainEvent.class);
        }

        @Test
        @DisplayName("Thanh toán đơn hàng thành công chuyển sang PAID và phát sinh OrderPaidDomainEvent")
        void testMarkPaidSuccess() {
            UUID studentId = UUID.randomUUID();
            List<OrderItem> items = List.of(
                    OrderItem.create(UUID.randomUUID(), UUID.randomUUID(), "Lập trình Java", new MoneyVnd(300_000))
            );
            Order order = Order.create(studentId, "ORD-PAY-1", "PAYOS", items, MoneyVnd.ZERO, 15);
            order.clearDomainEvents();

            order.markPaid("REF-CONFIRMED", 12345678L, Instant.now());

            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
            assertThat(order.getOrderCode()).isEqualTo(12345678L);
            assertThat(order.getPaidAt()).isNotNull();
            assertThat(order.getDomainEvents()).hasSize(1);
            assertThat(order.getDomainEvents().getFirst()).isInstanceOf(OrderPaidDomainEvent.class);
        }

        @Test
        @DisplayName("markPaid() có tính lũy đẳng (Idempotent) khi gọi lại trên đơn đã PAID")
        void testMarkPaidIdempotent() {
            UUID studentId = UUID.randomUUID();
            List<OrderItem> items = List.of(
                    OrderItem.create(UUID.randomUUID(), UUID.randomUUID(), "Khoa học Tự nhiên", new MoneyVnd(200_000))
            );
            Order order = Order.create(studentId, "ORD-IDEMPOTENT", "PAYOS", items, MoneyVnd.ZERO, 15);
            order.markPaid("REF-1", 111L, Instant.now());
            order.clearDomainEvents();

            // Gọi lại lần 2
            order.markPaid("REF-1", 111L, Instant.now());

            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
            assertThat(order.getDomainEvents()).isEmpty();
        }

        @Test
        @DisplayName("Đơn hàng quá hạn 15 phút sẽ ném ngoại lệ ORDER_EXPIRED khi thanh toán")
        void testMarkPaidExpiredFails() {
            UUID studentId = UUID.randomUUID();
            List<OrderItem> items = List.of(
                    OrderItem.create(UUID.randomUUID(), UUID.randomUUID(), "Tiếng Anh Giao Tiếp", new MoneyVnd(400_000))
            );
            Instant pastTime = Instant.now().minus(20, ChronoUnit.MINUTES);
            Order order = new Order(
                    UUID.randomUUID(),
                    studentId,
                    "ORD-EXPIRED",
                    null,
                    null,
                    "PAYOS",
                    OrderStatus.PENDING,
                    items,
                    new MoneyVnd(400_000),
                    MoneyVnd.ZERO,
                    pastTime.plus(15, ChronoUnit.MINUTES), // Hết hạn 5 phút trước
                    null,
                    pastTime,
                    pastTime
            );

            assertThatThrownBy(() -> order.markPaid("REF-LATE", 222L, Instant.now()))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("hết hạn thanh toán");
            assertThat(order.getStatus()).isEqualTo(OrderStatus.EXPIRED);
        }

        @Test
        @DisplayName("Không thể hủy đơn hàng đã thanh toán thành công")
        void testCannotCancelPaidOrder() {
            UUID studentId = UUID.randomUUID();
            List<OrderItem> items = List.of(
                    OrderItem.create(UUID.randomUUID(), UUID.randomUUID(), "Lịch Sử", new MoneyVnd(100_000))
            );
            Order order = Order.create(studentId, "ORD-PAID-CANCEL", "PAYOS", items, MoneyVnd.ZERO, 15);
            order.markPaid("REF-OK", 333L, Instant.now());

            assertThatThrownBy(() -> order.cancel("Muốn đổi ý"))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("đã thanh toán thành công, không thể hủy");
        }
    }

    @Nested
    @DisplayName("2. Kiểm thử Phân chia doanh thu (RevenueSplit & Ledger Invariants)")
    class RevenueSplitTests {

        @Test
        @DisplayName("Phân chia chuẩn 80% Giáo viên / 20% Nền tảng đảm bảo bất biến số dư")
        void testStandardRevenueSplit() {
            UUID teacherId = UUID.randomUUID();
            UUID studentId = UUID.randomUUID();
            UUID courseId = UUID.randomUUID();
            UUID orderId = UUID.randomUUID();
            UUID orderItemId = UUID.randomUUID();
            MoneyVnd gross = new MoneyVnd(1_000_000);

            RevenueSplit split = RevenueSplit.calculateStandard(
                    teacherId, studentId, courseId, orderId, orderItemId, gross, Instant.now()
            );

            assertThat(split.getPlatformFeePercent()).isEqualTo(20);
            assertThat(split.getTeacherPercent()).isEqualTo(80);
            assertThat(split.getPlatformFee()).isEqualTo(new MoneyVnd(200_000));
            assertThat(split.getTeacherAmount()).isEqualTo(new MoneyVnd(800_000));
            // Ledger invariant: gross == platformFee + teacherAmount
            assertThat(split.getGrossAmount().amount())
                    .isEqualTo(split.getPlatformFee().amount() + split.getTeacherAmount().amount());
        }

        @Test
        @DisplayName("Không cho phép tạo RevenueSplit nếu tổng tiền không khớp bất biến kế toán")
        void testRevenueSplitLedgerMismatchThrows() {
            UUID teacherId = UUID.randomUUID();
            UUID studentId = UUID.randomUUID();
            UUID courseId = UUID.randomUUID();
            UUID orderId = UUID.randomUUID();
            UUID orderItemId = UUID.randomUUID();

            assertThatThrownBy(() -> new RevenueSplit(
                    UUID.randomUUID(),
                    teacherId,
                    studentId,
                    courseId,
                    orderId,
                    orderItemId,
                    new MoneyVnd(1_000_000), // gross
                    new MoneyVnd(300_000),   // platformFee
                    new MoneyVnd(600_000),   // teacherAmount -> Tổng chỉ 900.000 != 1.000.000!
                    30,
                    70,
                    "2026-11",
                    null,
                    Instant.now()
            )).isInstanceOf(DomainException.class)
                    .hasMessageContaining("Doanh thu không khớp");
        }
    }

    @Nested
    @DisplayName("3. Kiểm thử Kỳ quyết toán cho Giáo viên (PayoutPeriod Aggregate)")
    class PayoutPeriodTests {

        @Test
        @DisplayName("Kỳ quyết toán cộng dồn giao dịch và Admin xác nhận chuyển khoản thành công")
        void testPayoutPeriodWorkflow() {
            UUID teacherId = UUID.randomUUID();
            UUID adminId = UUID.randomUUID();
            PayoutPeriod period = PayoutPeriod.create(teacherId, "2026-11");

            RevenueSplit split1 = RevenueSplit.calculateStandard(
                    teacherId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                    new MoneyVnd(500_000), Instant.now()
            );
            RevenueSplit split2 = RevenueSplit.calculateStandard(
                    teacherId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                    new MoneyVnd(500_000), Instant.now()
            );

            period.addSplit(split1);
            period.addSplit(split2);

            assertThat(period.getTransactionCount()).isEqualTo(2);
            assertThat(period.getTotalGross()).isEqualTo(new MoneyVnd(1_000_000));
            assertThat(period.getTotalPlatformFee()).isEqualTo(new MoneyVnd(200_000));
            assertThat(period.getTotalTeacherAmount()).isEqualTo(new MoneyVnd(800_000));

            // Chuyển sang PROCESSING
            period.startProcessing();
            assertThat(period.getStatus()).isEqualTo(PayoutStatus.PROCESSING);

            // Admin xác nhận chuyển khoản thành công (UC43)
            period.confirmPayout(adminId, "VCB-987654321", "Thanh toan doanh thu T11/2026", "https://cdn.beeacademy.com/unc/t11.pdf", Instant.now());

            assertThat(period.getStatus()).isEqualTo(PayoutStatus.PAID);
            assertThat(period.getPaidByAdmin()).isEqualTo(adminId);
            assertThat(period.getTransferRef()).isEqualTo("VCB-987654321");
            assertThat(period.getDomainEvents()).hasSize(1);
            assertThat(period.getDomainEvents().getFirst()).isInstanceOf(PayoutConfirmedDomainEvent.class);
        }
    }

    @Nested
    @DisplayName("4. Kiểm thử Tài khoản ngân hàng giáo viên (TeacherBankAccount Aggregate)")
    class BankAccountTests {

        @Test
        @DisplayName("Đổi số tài khoản ngân hàng sẽ kích hoạt kiểm toán và đưa về PENDING")
        void testUpdateBankDetailsAuditing() {
            UUID teacherId = UUID.randomUUID();
            TeacherBankAccount account = TeacherBankAccount.create(
                    teacherId, "Vietcombank", "0123456789", "NGUYEN VAN A", "Hanoi"
            );
            UUID adminId = UUID.randomUUID();
            account.verifyByAdmin(adminId);
            assertThat(account.getVerifyStatus()).isEqualTo(BankVerifyStatus.VERIFIED);

            // Giáo viên cập nhật số tài khoản mới
            account.updateAccountDetails("Techcombank", "9876543210", "NGUYEN VAN A", "Saigon", "NGUYEN VAN A", "Đổi sang nhận Techcombank");

            assertThat(account.getVerifyStatus()).isEqualTo(BankVerifyStatus.PENDING);
            assertThat(account.getBankName()).isEqualTo("Techcombank");
            assertThat(account.getAccountNumber()).isEqualTo("9876543210");
            assertThat(account.getAuditLogs()).hasSize(3); // Tạo ban đầu, Admin duyệt, Cập nhật mới
        }
    }

    @Nested
    @DisplayName("5. Kiểm thử Điểm thưởng & Voucher (RewardVoucher & StudentRewardBalance)")
    class RewardTests {

        @Test
        @DisplayName("Cộng và quy đổi điểm thưởng học sinh")
        void testStudentRewardBalanceOperations() {
            UUID studentId = UUID.randomUUID();
            StudentRewardBalance balance = StudentRewardBalance.init(studentId);

            balance.awardPoints(150, "Đạt điểm 10 kỳ thi Toán Cuối Kỳ");
            assertThat(balance.getPoints()).isEqualTo(150);
            assertThat(balance.getDomainEvents()).hasSize(1);
            assertThat(balance.getDomainEvents().getFirst()).isInstanceOf(RewardAwardedDomainEvent.class);

            balance.redeemPoints(50, "Đổi voucher giảm 20%");
            assertThat(balance.getPoints()).isEqualTo(100);

            // Quy đổi vượt quá số điểm hiện có -> lỗi
            assertThatThrownBy(() -> balance.redeemPoints(200, "Vượt quá điểm"))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("Không đủ điểm thưởng");
        }

        @Test
        @DisplayName("Voucher kiểm tra hạn dùng và áp dụng trần chiết khấu tối đa")
        void testRewardVoucherValidationAndDiscount() {
            UUID studentId = UUID.randomUUID();
            RewardVoucher voucher = RewardVoucher.createForStudent(
                    studentId,
                    "BEE20K",
                    "Giảm 20% tối đa 50k",
                    20,
                    new MoneyVnd(50_000), // trần tối đa 50.000 đ
                    Instant.now().plus(7, ChronoUnit.DAYS)
            );

            assertThat(voucher.isValid(Instant.now())).isTrue();

            // Đơn hàng 500.000 đ -> 20% là 100.000 đ, nhưng bị chặn trần 50.000 đ
            MoneyVnd discount = voucher.calculateDiscount(new MoneyVnd(500_000));
            assertThat(discount).isEqualTo(new MoneyVnd(50_000));

            // Học sinh sử dụng voucher
            voucher.use(studentId, Instant.now());
            assertThat(voucher.getStatus()).isEqualTo(RewardVoucherStatus.USED);

            // Không thể dùng lại voucher đã USED
            assertThatThrownBy(() -> voucher.use(studentId, Instant.now()))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("không còn hiệu lực");
        }
    }
}
