package com.beeacademy.backend.domain.learning;

import com.beeacademy.backend.domain.common.DomainEvent;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.common.ScorePercent;
import com.beeacademy.backend.domain.learning.event.CertificateIssuedEvent;
import com.beeacademy.backend.domain.learning.event.CourseCompletedEvent;
import com.beeacademy.backend.domain.learning.event.LessonCompletedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử Tầng Domain Learning (WatchSegments, Tiến độ 80%, Enrollment & Certificate)")
class LearningDomainTest {

    @Test
    @DisplayName("Thuật toán WatchSegments (UC14): Tự động gộp khoảng xem chồng lấp, chống gian lận lặp đoạn")
    void testWatchSegmentsMergingAndAntiCheat() {
        WatchSegments segments = WatchSegments.empty();

        // Học viên xem từ giây 0 đến 30
        segments.addSegment(0, 30);
        assertEquals(1, segments.getSegments().size());
        assertEquals(30, segments.calculateTotalUniqueSeconds());

        // Học viên xem tua lại từ giây 20 đến 80 -> Gộp thành [0, 80]
        segments.addSegment(20, 80);
        assertEquals(1, segments.getSegments().size());
        assertEquals(80, segments.calculateTotalUniqueSeconds());

        // Học viên nhảy cóc xem từ giây 100 đến 140 -> Có 2 khoảng: [0, 80] và [100, 140]
        segments.addSegment(100, 140);
        assertEquals(2, segments.getSegments().size());
        assertEquals(120, segments.calculateTotalUniqueSeconds()); // 80 + 40 = 120

        // Học viên xem đoạn kết nối từ 70 đến 110 -> Gộp toàn bộ thành 1 khoảng [0, 140]
        segments.addSegment(70, 110);
        assertEquals(1, segments.getSegments().size());
        assertEquals(140, segments.calculateTotalUniqueSeconds());

        // Gian lận xem đi xem lại đoạn [0, 10] -> Tổng số giây thực học không đổi
        segments.addSegment(0, 10);
        segments.addSegment(5, 15);
        assertEquals(1, segments.getSegments().size());
        assertEquals(140, segments.calculateTotalUniqueSeconds());
    }

    @Test
    @DisplayName("Tiến độ xem video (StudentVideoProgress): Chỉ hoàn thành khi xem đạt >= 80% thời lượng bài học (UC14)")
    void testStudentVideoProgressCompletionThreshold() {
        UUID studentId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        UUID lessonId = UUID.randomUUID();

        StudentVideoProgress progress = StudentVideoProgress.create(studentId, courseId, lessonId);
        int totalDurationSec = 1000; // Bài học dài 1000 giây -> Ngưỡng 80% là 800 giây
        Instant now = Instant.now();

        // Xem 500 giây đầu (50%) -> Chưa hoàn thành
        progress.recordWatch(0, 500, 500, totalDurationSec, now);
        assertFalse(progress.isCompleted());
        assertNull(progress.getCompletedAt());
        assertTrue(progress.pollDomainEvents().isEmpty());

        // Xem thêm đến 790 giây (79%) -> Vẫn chưa đạt 80%
        progress.recordWatch(500, 790, 790, totalDurationSec, now.plusSeconds(300));
        assertFalse(progress.isCompleted());
        assertTrue(progress.pollDomainEvents().isEmpty());

        // Xem tiếp từ 790 đến 810 giây (81%) -> Đạt và vượt 80% -> Đánh dấu completed và sinh LessonCompletedEvent
        Instant completionTime = now.plusSeconds(320);
        progress.recordWatch(790, 810, 810, totalDurationSec, completionTime);

        assertTrue(progress.isCompleted());
        assertEquals(completionTime, progress.getCompletedAt());
        assertEquals(810, progress.getLastPositionSec());

        List<DomainEvent> events = progress.pollDomainEvents();
        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof LessonCompletedEvent);
        LessonCompletedEvent event = (LessonCompletedEvent) events.get(0);
        assertEquals(studentId, event.studentId());
        assertEquals(courseId, event.courseId());
        assertEquals(lessonId, event.lessonId());
    }

    @Test
    @DisplayName("Ghi danh & Tiến độ khóa học (Enrollment): Tự động tính ScorePercent và kích hoạt CourseCompletedEvent khi đạt 100%")
    void testEnrollmentProgressCalculationAndCompletion() {
        UUID studentId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        Enrollment enrollment = Enrollment.create(studentId, courseId, orderId);
        assertEquals(ScorePercent.ZERO, enrollment.getProgressPercent());
        assertFalse(enrollment.isCompleted());

        int totalLessons = 5;
        Instant now = Instant.now();

        // Hoàn thành 2 / 5 bài học -> 40.0%
        enrollment.updateProgress(2, totalLessons, now);
        assertEquals(ScorePercent.of(40.0), enrollment.getProgressPercent());
        assertFalse(enrollment.isCompleted());
        assertTrue(enrollment.pollDomainEvents().isEmpty());

        // Hoàn thành 5 / 5 bài học -> 100.0% -> Đánh dấu hoàn thành khóa học & sinh CourseCompletedEvent
        Instant finishTime = now.plusSeconds(3600);
        enrollment.updateProgress(5, totalLessons, finishTime);

        assertEquals(ScorePercent.of(100.0), enrollment.getProgressPercent());
        assertTrue(enrollment.isCompleted());
        assertEquals(finishTime, enrollment.getCompletedAt());

        List<DomainEvent> events = enrollment.pollDomainEvents();
        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof CourseCompletedEvent);
        CourseCompletedEvent event = (CourseCompletedEvent) events.get(0);
        assertEquals(studentId, event.studentId());
        assertEquals(courseId, event.courseId());
    }

    @Test
    @DisplayName("Quy tắc cấp chứng chỉ (CertificateEligibilityPolicy - UC20): Bắt buộc 100% bài học và pass bài thi cuối khóa")
    void testCertificateEligibilityPolicyRules() {
        UUID studentId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        Enrollment enrollment = Enrollment.create(studentId, courseId, null);

        // Trường hợp 1: Chưa hoàn thành 100% bài học -> Ném DomainException COURSE_NOT_COMPLETED
        enrollment.updateProgress(3, 4, Instant.now()); // 75%
        assertFalse(CertificateEligibilityPolicy.isEligible(enrollment, true));

        DomainException ex1 = assertThrows(DomainException.class, () ->
                CertificateEligibilityPolicy.issueCertificate(enrollment, "Nguyễn Văn A", "Toán 12", true));
        assertEquals("COURSE_NOT_COMPLETED", ex1.getErrorCode());

        // Học viên hoàn thành 100% bài học
        enrollment.updateProgress(4, 4, Instant.now());
        assertTrue(enrollment.isCompleted());

        // Trường hợp 2: 100% bài học nhưng trượt bài thi cuối khóa -> Ném DomainException EXAM_NOT_PASSED
        assertFalse(CertificateEligibilityPolicy.isEligible(enrollment, false));

        DomainException ex2 = assertThrows(DomainException.class, () ->
                CertificateEligibilityPolicy.issueCertificate(enrollment, "Nguyễn Văn A", "Toán 12", false));
        assertEquals("EXAM_NOT_PASSED", ex2.getErrorCode());

        // Trường hợp 3: Đạt cả hai điều kiện -> Cấp chứng chỉ thành công và sinh CertificateIssuedEvent
        assertTrue(CertificateEligibilityPolicy.isEligible(enrollment, true));

        Certificate cert = CertificateEligibilityPolicy.issueCertificate(enrollment, "Nguyễn Văn A", "Toán 12", true);
        assertNotNull(cert.getId());
        assertTrue(cert.getCertificateCode().startsWith("BEE-CERT-"));
        assertEquals("Nguyễn Văn A", cert.getStudentName());
        assertEquals("Toán 12", cert.getCourseTitle());
        assertEquals(studentId, cert.getStudentId());
        assertEquals(courseId, cert.getCourseId());

        List<DomainEvent> events = cert.pollDomainEvents();
        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof CertificateIssuedEvent);
        CertificateIssuedEvent event = (CertificateIssuedEvent) events.get(0);
        assertEquals(cert.getId(), event.certificateId());
        assertEquals(cert.getCertificateCode(), event.certificateCode());
    }

    @Test
    @DisplayName("Hỏi & Đáp trong bài học (QaThread & QaMessage): Mở câu hỏi, thêm phản hồi và đánh dấu đã giải quyết")
    void testQaThreadWorkflow() {
        UUID studentId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        UUID lessonId = UUID.randomUUID();

        QaThread thread = QaThread.open(studentId, courseId, lessonId, "Công thức lượng giác ở phút 05:20", "Thầy giải thích giúp em đoạn này");
        assertNotNull(thread.getId());
        assertEquals("Công thức lượng giác ở phút 05:20", thread.getTitle());
        assertFalse(thread.isResolved());
        assertTrue(thread.getMessages().isEmpty());

        // Giáo viên gửi câu trả lời
        QaMessage reply = thread.addReply(teacherId, "Chào em, ở đây thầy áp dụng công thức nhân đôi sin(2x) = 2sin(x)cos(x)");
        assertNotNull(reply.getId());
        assertEquals(1, thread.getMessages().size());
        assertEquals("Chào em, ở đây thầy áp dụng công thức nhân đôi sin(2x) = 2sin(x)cos(x)", reply.getContent());

        // Học sinh cảm ơn và đánh dấu đã hiểu (resolved)
        thread.markResolved(true);
        assertTrue(thread.isResolved());
    }

    @Test
    @DisplayName("Ghi chú cá nhân trong video (StudentLessonNote): Ghi chú tại mốc thời gian và chỉnh sửa nội dung")
    void testStudentLessonNote() {
        UUID studentId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        UUID lessonId = UUID.randomUUID();

        StudentLessonNote note = StudentLessonNote.create(studentId, courseId, lessonId, 185, "Chú ý dạng bài này hay xuất hiện trong đề thi THPT");
        assertNotNull(note.getId());
        assertEquals(185, note.getVideoTimestampSec());
        assertEquals("Chú ý dạng bài này hay xuất hiện trong đề thi THPT", note.getContent());

        // Chỉnh sửa nội dung ghi chú
        note.updateContent("Bổ sung thêm công thức giải nhanh");
        assertEquals("Bổ sung thêm công thức giải nhanh", note.getContent());

        // Nội dung rỗng -> Ném lỗi
        assertThrows(DomainException.class, () -> note.updateContent("   "));
    }
}

