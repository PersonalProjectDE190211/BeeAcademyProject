package com.beeacademy.backend.domain.assessment;

import com.beeacademy.backend.domain.assessment.event.*;
import com.beeacademy.backend.domain.common.DomainEvent;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.common.ScorePercent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử Tầng Domain Assessment (Question Versioning, Quiz, Exam, Retake Cooldown & Assignment)")
class AssessmentDomainTest {

    @Test
    @DisplayName("Tạo mới câu hỏi: Kiểm tra tính hợp lệ của phương án trắc nghiệm (LaTeX/ảnh) và sinh sự kiện")
    void testQuestionCreationAndChoiceValidation() {
        UUID teacherId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID chapterId = UUID.randomUUID();

        List<QuestionChoice> choices = List.of(
                QuestionChoice.of(UUID.randomUUID(), "$x = 2$", null, true, 1),
                QuestionChoice.of(UUID.randomUUID(), "$x = -2$", null, false, 2),
                QuestionChoice.of(UUID.randomUUID(), "$x = 0$", "https://cdn.bee.edu.vn/graph1.png", false, 3),
                QuestionChoice.of(UUID.randomUUID(), "Vô nghiệm", null, false, 4)
        );

        Question question = Question.create(
                teacherId,
                categoryId,
                chapterId,
                8,
                "Tìm nghiệm của phương trình $x^2 - 4 = 0$ với $x > 0$:",
                "Ta có $x^2 = 4 \\Rightarrow x = \\pm 2$, vì $x > 0$ nên chọn $x = 2$",
                QuestionDifficulty.MEDIUM,
                QuestionType.MULTIPLE_CHOICE,
                List.of("phương trình", "lớp 8"),
                choices
        );

        assertNotNull(question.getId());
        assertEquals(1, question.getVersionNo());
        assertEquals(0, question.getUsageCount());
        assertEquals(QuestionStatus.ACTIVE, question.getStatus());
        assertEquals(4, question.getChoices().size());
        assertEquals(1, question.getAuditLogs().size());
        assertEquals("CREATED", question.getAuditLogs().get(0).getAction());

        List<DomainEvent> events = question.pollDomainEvents();
        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof QuestionCreatedEvent);

        // Vi phạm: ít hơn 2 lựa chọn
        assertThrows(DomainException.class, () -> Question.create(
                teacherId, categoryId, chapterId, 8, "Nội dung", "", QuestionDifficulty.EASY, QuestionType.MULTIPLE_CHOICE,
                List.of(), List.of(QuestionChoice.ofText(UUID.randomUUID(), "Đáp án A", true, 1))
        ));

        // Vi phạm: không có đáp án nào đúng
        assertThrows(DomainException.class, () -> Question.create(
                teacherId, categoryId, chapterId, 8, "Nội dung", "", QuestionDifficulty.EASY, QuestionType.MULTIPLE_CHOICE,
                List.of(), List.of(
                        QuestionChoice.ofText(UUID.randomUUID(), "A", false, 1),
                        QuestionChoice.ofText(UUID.randomUUID(), "B", false, 2)
                )
        ));
    }

    @Test
    @DisplayName("Cập nhật câu hỏi (UC33): Tự động lưu snapshot QuestionVersion, tăng versionNo và ghi audit log")
    void testQuestionVersioningOnUpdate() {
        UUID teacherId = UUID.randomUUID();
        Question question = Question.create(
                teacherId, UUID.randomUUID(), UUID.randomUUID(), 9,
                "Rút gọn biểu thức $A = (x+1)^2$", "Hằng đẳng thức số 1",
                QuestionDifficulty.EASY, QuestionType.MULTIPLE_CHOICE, List.of("toán 9"),
                List.of(
                        QuestionChoice.ofText(UUID.randomUUID(), "$x^2 + 2x + 1$", true, 1),
                        QuestionChoice.ofText(UUID.randomUUID(), "$x^2 + 1$", false, 2)
                )
        );
        question.clearDomainEvents();

        // Giáo viên cập nhật câu hỏi lên phiên bản mới
        List<QuestionChoice> newChoices = List.of(
                QuestionChoice.ofText(UUID.randomUUID(), "$x^2 + 2x + 1$", true, 1),
                QuestionChoice.ofText(UUID.randomUUID(), "$x^2 - 2x + 1$", false, 2),
                QuestionChoice.ofText(UUID.randomUUID(), "$x^2 + 1$", false, 3)
        );

        question.update(
                teacherId,
                "Rút gọn biểu thức $A = (x+1)^2$ và tìm điều kiện xác định",
                "Lời giải chi tiết mới",
                QuestionDifficulty.MEDIUM,
                List.of("toán 9", "nâng cao"),
                newChoices,
                "Bổ sung thêm 1 phương án nhiễu và chỉnh lại đề bài"
        );

        assertEquals(2, question.getVersionNo());
        assertEquals("Rút gọn biểu thức $A = (x+1)^2$ và tìm điều kiện xác định", question.getContent());
        assertEquals(QuestionDifficulty.MEDIUM, question.getDifficulty());

        // Kiểm tra snapshot phiên bản cũ (v1)
        assertEquals(1, question.getVersions().size());
        QuestionVersion v1 = question.getVersions().get(0);
        assertEquals(1, v1.getVersionNo());
        assertEquals("Rút gọn biểu thức $A = (x+1)^2$", v1.getContent());

        // Kiểm tra audit log
        assertEquals(2, question.getAuditLogs().size());
        assertEquals("UPDATED", question.getAuditLogs().get(1).getAction());
        assertEquals("Bổ sung thêm 1 phương án nhiễu và chỉnh lại đề bài", question.getAuditLogs().get(1).getDetails());

        List<DomainEvent> events = question.pollDomainEvents();
        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof QuestionUpdatedEvent);
        assertEquals(2, ((QuestionUpdatedEvent) events.get(0)).newVersionNo());

        // Kẻ lạ (khác teacherId) sửa câu hỏi -> ném UNAUTHORIZED_EDIT
        assertThrows(DomainException.class, () -> question.update(
                UUID.randomUUID(), "Hacking", "", QuestionDifficulty.HARD, List.of(), newChoices, "Lỗi"
        ));
    }

    @Test
    @DisplayName("Cấu hình Quiz (QuizConfig): Kiểm chứng bất biến easyCount + mediumCount + hardCount == totalQuestions")
    void testQuizConfigDistributionInvariant() {
        UUID chapterId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();

        // Hợp lệ: 3 dễ + 5 tb + 2 khó = 10 tổng
        QuizConfig config = QuizConfig.create(chapterId, teacherId, 10, 3, 5, 2, 30, ScorePercent.of(60.0), true, true, 3);
        assertEquals(10, config.getTotalQuestions());
        assertEquals(3, config.getEasyCount());
        assertEquals(5, config.getMediumCount());
        assertEquals(2, config.getHardCount());

        // Vi phạm: 3 dễ + 5 tb + 1 khó = 9 != 10 tổng -> Ném DomainException
        assertThrows(DomainException.class, () -> QuizConfig.create(
                chapterId, teacherId, 10, 3, 5, 1, 30, ScorePercent.of(60.0), true, true, 3
        ));

        // Vi phạm: tổng số câu <= 0
        assertThrows(DomainException.class, () -> QuizConfig.create(
                chapterId, teacherId, 0, 0, 0, 0, 30, ScorePercent.of(60.0), true, true, 3
        ));
    }

    @Test
    @DisplayName("Làm bài Quiz chương (QuizAttempt): Bắt đầu, nộp bài, chấm điểm đạt/trượt và chống nộp lại")
    void testQuizAttemptWorkflow() {
        UUID studentId = UUID.randomUUID();
        UUID chapterId = UUID.randomUUID();
        UUID configId = UUID.randomUUID();

        QuizAttempt attempt = QuizAttempt.start(studentId, configId, chapterId);
        assertEquals(AttemptStatus.IN_PROGRESS, attempt.getStatus());
        assertFalse(attempt.isPassed());

        // Nộp bài với điểm 80% (ngưỡng đạt là 60%)
        attempt.submit(ScorePercent.of(80.0), ScorePercent.of(60.0), Instant.now());
        assertEquals(AttemptStatus.SUBMITTED, attempt.getStatus());
        assertTrue(attempt.isPassed());
        assertEquals(80.0, attempt.getScore().value().doubleValue());

        List<DomainEvent> events = attempt.pollDomainEvents();
        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof QuizAttemptSubmittedEvent);

        // Nộp lại khi bài đã kết thúc -> Ném ATTEMPT_NOT_IN_PROGRESS
        assertThrows(DomainException.class, () -> attempt.submit(ScorePercent.of(90.0), ScorePercent.of(60.0), Instant.now()));
    }

    @Test
    @DisplayName("Làm bài thi chính thức (ExamAttempt): Ghi nhận vi phạm liêm chính thi cử (UC44)")
    void testExamAttemptAndIntegrityEvents() {
        UUID studentId = UUID.randomUUID();
        UUID examConfigId = UUID.randomUUID();

        ExamAttempt attempt = ExamAttempt.start(studentId, examConfigId, 1);
        assertEquals(AttemptStatus.IN_PROGRESS, attempt.getStatus());
        assertTrue(attempt.getIntegrityEvents().isEmpty());

        // Ghi nhận học sinh chuyển tab và thoát toàn màn hình
        attempt.recordIntegrityEvent(IntegrityEventType.TAB_SWITCH, "Chuyển sang tab Google Search 5 giây");
        attempt.recordIntegrityEvent(IntegrityEventType.FULLSCREEN_EXIT, "Thoát toàn màn hình tại phút thứ 12");
        assertEquals(2, attempt.getIntegrityEvents().size());
        assertEquals(IntegrityEventType.TAB_SWITCH, attempt.getIntegrityEvents().get(0).getEventType());

        // Nộp bài
        attempt.submit(ScorePercent.of(85.0), ScorePercent.of(70.0), "{\"q1\": \"A\", \"q2\": \"C\"}", Instant.now());
        assertEquals(AttemptStatus.SUBMITTED, attempt.getStatus());
        assertTrue(attempt.isPassed());

        assertTrue(attempt.pollDomainEvents().stream().anyMatch(e -> e instanceof ExamAttemptSubmittedEvent));
    }

    @Test
    @DisplayName("Quy định xin thi lại (ExamRetakeRequest - UC17-19): Tối đa 5 lần, Cooldown 12 giờ nếu bị từ chối")
    void testExamRetakeRequestStrictRules() {
        UUID studentId = UUID.randomUUID();
        UUID examConfigId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();
        Instant now = Instant.now();

        // 1. Học sinh gửi đơn xin thi lại lần 1 -> Thành công
        ExamRetakeRequest request1 = ExamRetakeRequest.createRequest(studentId, examConfigId, "Mạng bị chập chờn khi đang thi", 0, null, now);
        assertEquals(1, request1.getRequestCount());
        assertEquals(RetakeRequestStatus.PENDING, request1.getStatus());

        // 2. Giáo viên từ chối lần 1 -> Kích hoạt Cooldown 12 giờ
        request1.reject(teacherId, "Chưa đủ lý do thuyết phục", now);
        assertEquals(RetakeRequestStatus.REJECTED, request1.getStatus());
        assertNotNull(request1.getRejectionCooldownUntil());
        assertEquals(now.plus(Duration.ofHours(12)), request1.getRejectionCooldownUntil());

        // 3. Học sinh cố gửi lại sau 2 giờ (vẫn trong cooldown 12h) -> Bị từ chối (RETAKE_COOLDOWN_ACTIVE)
        Instant after2Hours = now.plus(2, ChronoUnit.HOURS);
        DomainException exCooldown = assertThrows(DomainException.class, () ->
                ExamRetakeRequest.createRequest(studentId, examConfigId, "Xin thầy xem xét lại", 1, request1.getRejectionCooldownUntil(), after2Hours));
        assertEquals("RETAKE_COOLDOWN_ACTIVE", exCooldown.getErrorCode());

        // 4. Sau 13 giờ (hết cooldown) -> Gửi lại lần 2 thành công
        Instant after13Hours = now.plus(13, ChronoUnit.HOURS);
        ExamRetakeRequest request2 = ExamRetakeRequest.createRequest(studentId, examConfigId, "Em bổ sung ảnh chụp modem lỗi mạng", 1, request1.getRejectionCooldownUntil(), after13Hours);
        assertEquals(2, request2.getRequestCount());

        // 5. Giáo viên duyệt cho thi lại trong 24 giờ
        request2.approve(teacherId, Duration.ofHours(24), "Duyệt mở thêm 1 lượt thi trong 24h", after13Hours);
        assertEquals(RetakeRequestStatus.APPROVED, request2.getStatus());
        assertNotNull(request2.getGrantExpiresAt());

        // 6. Kiểm tra hết hạn: Sau 25 giờ học sinh vẫn chưa làm bài -> Trạng thái chuyển EXPIRED
        request2.checkExpiration(after13Hours.plus(25, ChronoUnit.HOURS));
        assertEquals(RetakeRequestStatus.EXPIRED, request2.getStatus());

        // 7. Học sinh đã dùng hết 5 lần xin thi lại -> Lần thứ 6 bị chặn vĩnh viễn (MAX_RETAKE_EXCEEDED)
        DomainException exLimit = assertThrows(DomainException.class, () ->
                ExamRetakeRequest.createRequest(studentId, examConfigId, "Lần thứ 6", 5, null, now));
        assertEquals("MAX_RETAKE_EXCEEDED", exLimit.getErrorCode());
    }

    @Test
    @DisplayName("Bài nộp tự luận & Chấm điểm (AssignmentSubmission): Chấm điểm bởi GV/AI, lưu vết GradeAuditLog")
    void testAssignmentSubmissionAndGrading() {
        UUID studentId = UUID.randomUUID();
        UUID assignmentId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();
        UUID aiId = UUID.randomUUID();

        // 1. Học sinh nộp bài
        AssignmentSubmission submission = AssignmentSubmission.submit(
                studentId, assignmentId, "Bài giải chi tiết câu hình học...", "https://cdn.bee.edu.vn/bai-nop.pdf"
        );
        assertEquals(SubmissionStatus.SUBMITTED, submission.getStatus());
        assertNull(submission.getScore());
        assertTrue(submission.getGradeAuditLogs().isEmpty());

        // 2. AI chấm sơ bộ trước
        Instant t1 = Instant.now();
        submission.grade(aiId, true, ScorePercent.of(75.0), "AI: Lời giải đúng nhưng thiếu vẽ hình", t1);
        assertEquals(SubmissionStatus.GRADED, submission.getStatus());
        assertEquals(75.0, submission.getScore().value().doubleValue());
        assertEquals(1, submission.getGradeAuditLogs().size());
        assertTrue(submission.getGradeAuditLogs().get(0).isAiGraded());

        // 3. Giáo viên chấm lại và điều chỉnh điểm
        Instant t2 = t1.plusSeconds(300);
        submission.grade(teacherId, false, ScorePercent.of(85.0), "GV: Thầy cộng thêm 1 điểm do cách giải sáng tạo", t2);
        assertEquals(85.0, submission.getScore().value().doubleValue());
        assertEquals(2, submission.getGradeAuditLogs().size());
        assertFalse(submission.getGradeAuditLogs().get(1).isAiGraded());
        assertEquals("GV: Thầy cộng thêm 1 điểm do cách giải sáng tạo", submission.getGradeAuditLogs().get(1).getFeedback());

        List<DomainEvent> events = submission.pollDomainEvents();
        assertEquals(3, events.size()); // 1 submit + 2 grade
        assertTrue(events.get(0) instanceof AssignmentSubmittedEvent);
        assertTrue(events.get(1) instanceof AssignmentGradedEvent);
        assertTrue(events.get(2) instanceof AssignmentGradedEvent);
    }
}
