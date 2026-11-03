package com.beeacademy.backend.domain.course;

import com.beeacademy.backend.domain.common.DomainEvent;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.common.MoneyVnd;
import com.beeacademy.backend.domain.course.event.CourseApprovedEvent;
import com.beeacademy.backend.domain.course.event.CourseRejectedEvent;
import com.beeacademy.backend.domain.course.event.CourseRevisionRequestedEvent;
import com.beeacademy.backend.domain.course.event.CourseSubmittedForReviewEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử Tầng Domain Course (Course Aggregate, State Machine & Invariants)")
class CourseDomainTest {

    @Test
    @DisplayName("Tạo mới khóa học: Trạng thái ban đầu DRAFT, chỉ số đếm = 0, tự sinh slug")
    void testCreateDraftCourse() {
        UUID teacherId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        Course course = Course.create(
                teacherId,
                categoryId,
                "Khóa Học Luyện Thi Toán Lớp 12 Nâng Cao",
                "Mô tả chi tiết",
                "https://cdn.bee.edu.vn/thumb.jpg",
                MoneyVnd.of(499000),
                List.of(12)
        );

        assertNotNull(course.getId());
        assertEquals(CourseStatus.DRAFT, course.getStatus());
        assertEquals("khoa-hoc-luyen-thi-toan-lop-12-nang-cao", course.getSlug().value());
        assertEquals(0, course.getTotalChapters());
        assertEquals(0, course.getTotalLessons());
        assertEquals(0, course.getTotalDurationSec());
        assertEquals(499000L, course.getPrice().amount());
        assertTrue(course.getApprovalHistories().isEmpty());
    }

    @Test
    @DisplayName("Thêm/xóa Chương và Bài học: Tự động bảo toàn Invariants (totalChapters, totalLessons, totalDurationSec)")
    void testAddChaptersAndLessonsRecalculatesInvariants() {
        Course course = Course.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Lập trình Java Căn Bản",
                "Mô tả",
                "",
                MoneyVnd.of(199000),
                List.of(10, 11, 12)
        );

        // Thêm Chương 1
        Chapter c1 = course.addChapter("Chương 1: Giới thiệu cú pháp", "Nhập môn");
        assertEquals(1, course.getTotalChapters());

        // Thêm 2 bài học vào Chương 1: Bài 1 (600s), Bài 2 (900s)
        course.addLesson(c1.getId(), "Bài 1.1: Hello World", "https://video.com/1", 600, true);
        Lesson l2 = course.addLesson(c1.getId(), "Bài 1.2: Biến và Kiểu dữ liệu", "https://video.com/2", 900, false);

        assertEquals(2, course.getTotalLessons());
        assertEquals(1500, course.getTotalDurationSec());

        // Thêm Chương 2 với 1 bài học (1200s)
        Chapter c2 = course.addChapter("Chương 2: Hướng đối tượng OOP", "OOP Căn bản");
        course.addLesson(c2.getId(), "Bài 2.1: Class và Object", "https://video.com/3", 1200, false);

        assertEquals(2, course.getTotalChapters());
        assertEquals(3, course.getTotalLessons());
        assertEquals(2700, course.getTotalDurationSec());

        // Xóa Bài 1.2 khỏi Chương 1
        course.removeLesson(c1.getId(), l2.getId());
        assertEquals(2, course.getTotalLessons());
        assertEquals(1800, course.getTotalDurationSec());

        // Xóa Chương 2
        course.removeChapter(c2.getId());
        assertEquals(1, course.getTotalChapters());
        assertEquals(1, course.getTotalLessons());
        assertEquals(600, course.getTotalDurationSec());
    }

    @Test
    @DisplayName("Nộp duyệt (submitForReview): Bắt buộc khóa học phải có ít nhất 1 bài học")
    void testSubmitForReviewRequiresAtLeastOneLesson() {
        Course course = Course.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Khóa học trống",
                "Mô tả",
                "",
                MoneyVnd.ZERO,
                List.of(10)
        );

        // Chưa có bài học -> ném DomainException
        DomainException ex = assertThrows(DomainException.class, course::submitForReview);
        assertEquals("COURSE_HAS_NO_LESSONS", ex.getErrorCode());
    }

    @Test
    @DisplayName("Luồng kiểm duyệt thành công: DRAFT -> submitForReview() -> PENDING_REVIEW -> approve() -> PUBLISHED")
    void testCourseApprovalWorkflow() {
        Course course = Course.create(UUID.randomUUID(), UUID.randomUUID(), "Vật lý 10", "", "", MoneyVnd.ZERO, List.of(10));
        Chapter c = course.addChapter("Chương 1", "");
        course.addLesson(c.getId(), "Bài 1", "url", 300, true);

        // Nộp duyệt
        course.submitForReview();
        assertEquals(CourseStatus.PENDING_REVIEW, course.getStatus());
        List<DomainEvent> events1 = course.pollDomainEvents();
        assertEquals(1, events1.size());
        assertTrue(events1.get(0) instanceof CourseSubmittedForReviewEvent);
        course.clearDomainEvents();

        // Admin phê duyệt
        UUID adminId = UUID.randomUUID();
        course.approve(adminId, "Khóa học rất chất lượng, duyệt ngay");

        assertEquals(CourseStatus.PUBLISHED, course.getStatus());
        assertNotNull(course.getPublishedAt());
        assertEquals(1, course.getApprovalHistories().size());
        assertEquals(CourseStatus.PUBLISHED, course.getApprovalHistories().get(0).getAction());
        assertEquals("Khóa học rất chất lượng, duyệt ngay", course.getApprovalHistories().get(0).getComment());

        List<DomainEvent> events2 = course.pollDomainEvents();
        assertEquals(1, events2.size());
        assertTrue(events2.get(0) instanceof CourseApprovedEvent);
        course.clearDomainEvents();
    }

    @Test
    @DisplayName("Luồng từ chối và yêu cầu sửa đổi: PENDING_REVIEW -> reject() -> DRAFT/NEEDS_REVISION")
    void testCourseRejectionAndRevisionWorkflow() {
        Course course = Course.create(UUID.randomUUID(), UUID.randomUUID(), "Hóa học 11", "", "", MoneyVnd.ZERO, List.of(11));
        Chapter c = course.addChapter("Chương 1", "");
        course.addLesson(c.getId(), "Bài 1", "url", 300, true);

        course.submitForReview();

        // Admin yêu cầu chỉnh sửa (NEEDS_REVISION)
        UUID adminId = UUID.randomUUID();
        course.requestRevision(adminId, "Âm thanh video bài 1 bị rè, vui lòng tải lại");
        assertEquals(CourseStatus.NEEDS_REVISION, course.getStatus());
        assertTrue(course.pollDomainEvents().stream().anyMatch(e -> e instanceof CourseRevisionRequestedEvent));

        // Giáo viên sửa xong và nộp duyệt lại
        course.submitForReview();
        assertEquals(CourseStatus.PENDING_REVIEW, course.getStatus());

        // Lần này Admin từ chối hẳn (REJECTED)
        course.reject(adminId, "Vi phạm bản quyền nội dung sách");
        assertEquals(CourseStatus.REJECTED, course.getStatus());
        assertTrue(course.pollDomainEvents().stream().anyMatch(e -> e instanceof CourseRejectedEvent));
        assertEquals(2, course.getApprovalHistories().size());
    }

    @Test
    @DisplayName("Chống vi phạm trạng thái: Không thể phê duyệt khi chưa ở PENDING_REVIEW")
    void testInvalidStateTransitions() {
        Course course = Course.create(UUID.randomUUID(), UUID.randomUUID(), "Sinh học 12", "", "", MoneyVnd.ZERO, List.of(12));

        // Đang ở DRAFT mà gọi approve() -> lỗi
        assertThrows(DomainException.class, () -> course.approve(UUID.randomUUID(), "Duyệt!"));

        // Đang ở DRAFT mà gọi reject() -> lỗi
        assertThrows(DomainException.class, () -> course.reject(UUID.randomUUID(), "Từ chối!"));
    }

    @Test
    @DisplayName("Kiểm thử Đánh giá khóa học (CourseReview): Điểm 1-5 sao và cập nhật")
    void testCourseReviewInvariants() {
        UUID courseId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();

        CourseReview review = CourseReview.create(courseId, studentId, 5, "Khóa học rất hay!");
        assertEquals(5, review.getRating());
        assertEquals("Khóa học rất hay!", review.getComment());
        assertFalse(review.isEdited());

        // Đánh giá lại 4 sao
        review.edit(4, "Bài tập hơi khó một chút");
        assertEquals(4, review.getRating());
        assertTrue(review.isEdited());

        // Điểm đánh giá không hợp lệ (< 1 hoặc > 5)
        assertThrows(DomainException.class, () -> CourseReview.create(courseId, studentId, 0, "Quá tệ"));
        assertThrows(DomainException.class, () -> CourseReview.create(courseId, studentId, 6, "Quá xuất sắc"));
    }
}
