package com.beeacademy.backend.application.course;

import com.beeacademy.backend.application.course.dto.CourseReviewResult;
import com.beeacademy.backend.application.course.dto.SubmitReviewCommand;
import com.beeacademy.backend.application.course.usecase.ManageCourseReviewUseCase;
import com.beeacademy.backend.domain.course.Course;
import com.beeacademy.backend.domain.course.CourseRepositoryPort;
import com.beeacademy.backend.domain.course.CourseReview;
import com.beeacademy.backend.domain.course.CourseReviewRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DisplayName("ManageCourseReviewUseCase Unit Tests (UC08)")
class ManageCourseReviewUseCaseTest {

    private CourseReviewRepositoryPort reviewRepository;
    private CourseRepositoryPort courseRepository;
    private ManageCourseReviewUseCase useCase;

    @BeforeEach
    void setUp() {
        reviewRepository = Mockito.mock(CourseReviewRepositoryPort.class);
        courseRepository = Mockito.mock(CourseRepositoryPort.class);
        useCase = new ManageCourseReviewUseCase(reviewRepository, courseRepository);
    }

    @Test
    @DisplayName("Học viên gửi đánh giá 5 sao -> Tự động cập nhật điểm trung bình và số lượng đánh giá của khóa học")
    void submitReview_Success() {
        UUID courseId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        Course course = Course.create(UUID.randomUUID(), UUID.randomUUID(), "Toán 10", "Hàm số", null, null, List.of(10));

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(reviewRepository.findByCourseIdAndStudentId(courseId, studentId)).thenReturn(Optional.empty());
        when(reviewRepository.save(any(CourseReview.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CourseReview r1 = CourseReview.create(courseId, studentId, 5, "Khóa học rất hay!");
        when(reviewRepository.findByCourseId(courseId)).thenReturn(List.of(r1));

        SubmitReviewCommand cmd = new SubmitReviewCommand(courseId, studentId, 5, "Khóa học rất hay!");
        CourseReviewResult result = useCase.submitReview(cmd);

        assertThat(result).isNotNull();
        assertThat(result.rating()).isEqualTo(5);
        assertThat(result.comment()).isEqualTo("Khóa học rất hay!");

        // Khóa học phải được cập nhật avgRating = 5.0 và totalReviews = 1
        assertThat(course.getAverageRating()).isEqualTo(BigDecimal.valueOf(5.0));
        assertThat(course.getTotalReviews()).isEqualTo(1);
    }
}
