package com.beeacademy.backend.application.course;

import com.beeacademy.backend.application.course.dto.CourseDetailResult;
import com.beeacademy.backend.application.course.dto.CourseSummaryResult;
import com.beeacademy.backend.application.course.usecase.ReviewCourseApprovalUseCase;
import com.beeacademy.backend.application.course.usecase.SubmitCourseForReviewUseCase;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.course.Chapter;
import com.beeacademy.backend.domain.course.Course;
import com.beeacademy.backend.domain.course.CourseRepositoryPort;
import com.beeacademy.backend.domain.course.CourseStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DisplayName("Submit & Review Course Approval Use Cases Test (UC30, UC39, UC41)")
class SubmitAndReviewApprovalUseCaseTest {

    private CourseRepositoryPort courseRepository;
    private SubmitCourseForReviewUseCase submitUseCase;
    private ReviewCourseApprovalUseCase reviewUseCase;

    @BeforeEach
    void setUp() {
        courseRepository = Mockito.mock(CourseRepositoryPort.class);
        submitUseCase = new SubmitCourseForReviewUseCase(courseRepository);
        reviewUseCase = new ReviewCourseApprovalUseCase(courseRepository);
    }

    @Test
    @DisplayName("Nộp duyệt khóa học thất bại nếu chưa có bài học nào")
    void submitForReview_FailsWhenNoLessons() {
        UUID teacherId = UUID.randomUUID();
        Course course = Course.create(teacherId, UUID.randomUUID(), "Lịch Sử 9", "Khởi nghĩa Lam Sơn", null, null, List.of(9));

        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> submitUseCase.execute(course.getId(), teacherId))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("ít nhất 1 bài học");
    }

    @Test
    @DisplayName("Quy trình hoàn chỉnh: Nộp duyệt -> Admin Phê duyệt xuất bản (PUBLISHED)")
    void fullApprovalFlow_Success() {
        UUID teacherId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        Course course = Course.create(teacherId, UUID.randomUUID(), "Địa Lý 10", "Địa hình Việt Nam", null, null, List.of(10));
        Chapter ch = course.addChapter("Chương 1", "Tổng quan");
        course.addLesson(ch.getId(), "Bài 1", "https://video.com/1.mp4", 600, true);

        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 1. Giáo viên nộp duyệt
        CourseDetailResult submitted = submitUseCase.execute(course.getId(), teacherId);
        assertThat(submitted.status()).isEqualTo(CourseStatus.PENDING_REVIEW.name());

        // 2. Admin xem danh sách chờ duyệt
        when(courseRepository.findByStatus(CourseStatus.PENDING_REVIEW)).thenReturn(List.of(course));
        List<CourseSummaryResult> pendingList = reviewUseCase.getPendingCourses();
        assertThat(pendingList).hasSize(1);

        // 3. Admin phê duyệt
        CourseDetailResult approved = reviewUseCase.approve(course.getId(), adminId, "Nội dung đạt chuẩn chất lượng");
        assertThat(approved.status()).isEqualTo(CourseStatus.PUBLISHED.name());
        assertThat(approved.publishedAt()).isNotNull();
    }

    @Test
    @DisplayName("Admin từ chối khóa học (REJECTED) hoặc yêu cầu sửa (NEEDS_REVISION)")
    void rejectAndRequestRevision_Success() {
        UUID teacherId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        Course course = Course.create(teacherId, UUID.randomUUID(), "Văn 12", "Nghị luận văn học", null, null, List.of(12));
        Chapter ch = course.addChapter("Chương 1", "Giới thiệu");
        course.addLesson(ch.getId(), "Bài 1", "https://video.com/1.mp4", 600, false);
        course.submitForReview();

        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Test request revision
        CourseDetailResult revised = reviewUseCase.requestRevision(course.getId(), adminId, "Chất lượng âm thanh chưa rõ ràng");
        assertThat(revised.status()).isEqualTo(CourseStatus.NEEDS_REVISION.name());

        // Giáo viên nộp lại
        course.submitForReview();

        // Admin từ chối hẳn
        CourseDetailResult rejected = reviewUseCase.reject(course.getId(), adminId, "Nội dung sao chép bản quyền");
        assertThat(rejected.status()).isEqualTo(CourseStatus.REJECTED.name());
    }
}

