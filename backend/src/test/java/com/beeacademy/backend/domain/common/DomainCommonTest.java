package com.beeacademy.backend.domain.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử Tầng Domain Base & Common Value Objects")
class DomainCommonTest {

    @Test
    @DisplayName("MoneyVnd: Đảm bảo số tiền không âm và tính toán doanh thu 80/20 chính xác")
    void testMoneyVnd() {
        MoneyVnd price = MoneyVnd.of(500_000);
        assertEquals(500_000, price.amount());
        assertEquals("500.000 đ", price.toString());

        // Phép cộng
        MoneyVnd bonus = MoneyVnd.of(100_000);
        assertEquals(600_000, price.plus(bonus).amount());

        // Phép trừ có chặn cận 0
        MoneyVnd largeDiscount = MoneyVnd.of(700_000);
        assertEquals(0, price.minusOrZero(largeDiscount).amount());

        // Chia doanh thu chuẩn 80% Giáo viên / 20% Nền tảng
        MoneyVnd platformFee = price.calculatePlatformFee(20);
        assertEquals(100_000, platformFee.amount());

        MoneyVnd teacherShare = price.calculateTeacherShare(20);
        assertEquals(400_000, teacherShare.amount());

        // Số tiền âm phải ném DomainException
        assertThrows(DomainException.class, () -> new MoneyVnd(-1000));
    }

    @Test
    @DisplayName("ScorePercent: Đảm bảo điểm số 0.0 - 100.0, làm tròn nửa bước và kiểm tra đạt/trượt")
    void testScorePercent() {
        ScorePercent score = ScorePercent.of(85.46);
        assertEquals(BigDecimal.valueOf(85.5), score.value());
        assertEquals("85.5%", score.toString());

        // Tính từ số câu đúng: 8/10 câu = 80.0%
        ScorePercent quizScore = ScorePercent.fromRatio(8, 10);
        assertEquals(BigDecimal.valueOf(80.0).setScale(1), quizScore.value());

        // Kiểm tra ngưỡng đạt (Pass score: 70.0%)
        ScorePercent passThreshold = ScorePercent.of(70.0);
        assertTrue(quizScore.isPassed(passThreshold));

        ScorePercent highThreshold = ScorePercent.of(90.0);
        assertFalse(quizScore.isPassed(highThreshold));

        // Điểm âm hoặc vượt quá 100 phải ném DomainException
        assertThrows(DomainException.class, () -> ScorePercent.of(-1.0));
        assertThrows(DomainException.class, () -> ScorePercent.of(100.1));
    }

    @Test
    @DisplayName("CourseSlug: Đảm bảo chuyển hóa tiếng Việt có dấu và ký tự đặc biệt sang URL slug chuẩn")
    void testCourseSlug() {
        String title = "Toán Học Lớp 8: Đa Thức & Hằng Đẳng Thức Đáng Nhớ!";
        CourseSlug slug = CourseSlug.fromTitle(title);

        assertEquals("toan-hoc-lop-8-da-thuc-hang-dang-thuc-dang-nho", slug.value());

        // Tiêu đề rỗng fallback về "course"
        assertEquals("course", CourseSlug.fromTitle("   ").value());
    }

    @Test
    @DisplayName("AbstractAggregateRoot: Đăng ký sự kiện, kiểm tra danh sách và dọn sạch sự kiện")
    void testAbstractAggregateRoot() {
        TestAggregate aggregate = new TestAggregate();
        assertTrue(aggregate.pollDomainEvents().isEmpty());

        TestEvent event = new TestEvent(UUID.randomUUID(), Instant.now());
        aggregate.raise(event);

        List<DomainEvent> events = aggregate.pollDomainEvents();
        assertEquals(1, events.size());
        assertEquals(event.getEventId(), events.get(0).getEventId());

        aggregate.clearDomainEvents();
        assertTrue(aggregate.pollDomainEvents().isEmpty());
    }

    @Test
    @DisplayName("AuditLog: Tạo bản ghi kiểm toán tập trung bất biến cho các thực thể nhạy cảm")
    void testCentralizedAuditLog() {
        UUID entityId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        AuditLog log = AuditLog.record(
                AuditEntityType.TEACHER_BANK_ACCOUNT,
                entityId,
                "update_bank_account",
                actorId,
                "teacher",
                "Đổi ngân hàng nhận lương",
                "{\"oldBank\":\"VCB\",\"newBank\":\"TCB\"}"
        );

        assertNotNull(log.getId());
        assertEquals(AuditEntityType.TEACHER_BANK_ACCOUNT, log.getEntityType());
        assertEquals("UPDATE_BANK_ACCOUNT", log.getAction());
        assertEquals("TEACHER", log.getActorRole());
        assertEquals("{\"oldBank\":\"VCB\",\"newBank\":\"TCB\"}", log.getDiffJson());
    }

    static class TestAggregate extends AbstractAggregateRoot {
        void raise(DomainEvent event) {
            registerEvent(event);
        }
    }

    record TestEvent(UUID eventId, Instant occurredAt) implements DomainEvent {
        @Override
        public UUID getEventId() {
            return eventId;
        }

        @Override
        public Instant getOccurredAt() {
            return occurredAt;
        }
    }
}
