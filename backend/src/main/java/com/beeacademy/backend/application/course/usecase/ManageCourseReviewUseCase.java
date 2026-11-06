package com.beeacademy.backend.application.course.usecase;

import com.beeacademy.backend.application.course.dto.CourseReviewResult;
import com.beeacademy.backend.application.course.dto.SubmitReviewCommand;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.course.Course;
import com.beeacademy.backend.domain.course.CourseRepositoryPort;
import com.beeacademy.backend.domain.course.CourseReview;
import com.beeacademy.backend.domain.course.CourseReviewRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Use case Đánh giá khóa học (UC07, UC08).
 * Quản lý gửi đánh giá từ 1 đến 5 sao, bình luận và tự động tính lại điểm trung bình khóa học.
 */
@Service
public class ManageCourseReviewUseCase {

    private final CourseReviewRepositoryPort reviewRepository;
    private final CourseRepositoryPort courseRepository;

    public ManageCourseReviewUseCase(CourseReviewRepositoryPort reviewRepository,
                                     CourseRepositoryPort courseRepository) {
        this.reviewRepository = reviewRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional
    public CourseReviewResult submitReview(SubmitReviewCommand command) {
        if (command == null) {
            throw new DomainException("INVALID_COMMAND", "Dữ liệu đánh giá không được rỗng");
        }

        Course course = courseRepository.findById(command.courseId())
                .orElseThrow(() -> new DomainException("COURSE_NOT_FOUND", "Không tìm thấy khóa học"));

        Optional<CourseReview> existingReview = reviewRepository.findByCourseIdAndStudentId(command.courseId(), command.studentId());
        CourseReview review;
        if (existingReview.isPresent()) {
            review = existingReview.get();
            review.edit(command.rating(), command.comment());
        } else {
            review = CourseReview.create(command.courseId(), command.studentId(), command.rating(), command.comment());
        }

        CourseReview savedReview = reviewRepository.save(review);

        // Tính lại điểm trung bình và tổng số đánh giá của khóa học
        List<CourseReview> allReviews = reviewRepository.findByCourseId(command.courseId());
        int totalReviews = allReviews.size();
        double avg = allReviews.stream().mapToInt(CourseReview::getRating).average().orElse(0.0);
        BigDecimal averageRating = BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP);

        course.updateReviewStats(averageRating, totalReviews);
        courseRepository.save(course);

        return CourseReviewResult.from(savedReview);
    }

    @Transactional(readOnly = true)
    public List<CourseReviewResult> getCourseReviews(UUID courseId) {
        return reviewRepository.findByCourseId(courseId).stream()
                .map(CourseReviewResult::from)
                .toList();
    }
}

