package com.beeacademy.backend.application.learning.usecase;

import com.beeacademy.backend.application.learning.dto.EnrollmentSummaryResult;
import com.beeacademy.backend.domain.course.Course;
import com.beeacademy.backend.domain.course.CourseRepositoryPort;
import com.beeacademy.backend.domain.learning.Enrollment;
import com.beeacademy.backend.domain.learning.EnrollmentRepositoryPort;
import com.beeacademy.backend.domain.learning.StudentVideoProgressRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Use case Lấy danh sách khóa học học viên đã đăng ký (UC13).
 */
@Service
public class GetMyEnrollmentsUseCase {

    private final EnrollmentRepositoryPort enrollmentRepository;
    private final CourseRepositoryPort courseRepository;
    private final StudentVideoProgressRepositoryPort progressRepository;

    public GetMyEnrollmentsUseCase(EnrollmentRepositoryPort enrollmentRepository,
                                   CourseRepositoryPort courseRepository,
                                   StudentVideoProgressRepositoryPort progressRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.progressRepository = progressRepository;
    }

    @Transactional(readOnly = true)
    public List<EnrollmentSummaryResult> execute(UUID studentId) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        List<EnrollmentSummaryResult> results = new ArrayList<>();

        for (Enrollment enrollment : enrollments) {
            Optional<Course> courseOpt = courseRepository.findById(enrollment.getCourseId());
            String title = courseOpt.map(Course::getTitle).orElse("Khóa học không xác định");
            String slug = courseOpt.map(c -> c.getSlug().value()).orElse("");
            String thumb = courseOpt.map(Course::getThumbnailUrl).orElse("");
            int totalLessons = courseOpt.map(Course::getTotalLessons).orElse(0);

            long completedCount = progressRepository.countCompletedLessons(studentId, enrollment.getCourseId());
            double progressPercent = enrollment.getProgressPercent() != null
                    ? enrollment.getProgressPercent().value().doubleValue()
                    : 0.0;

            results.add(new EnrollmentSummaryResult(
                    enrollment.getId(),
                    enrollment.getCourseId(),
                    title,
                    slug,
                    thumb,
                    (int) completedCount,
                    totalLessons,
                    progressPercent,
                    enrollment.isCompleted(),
                    enrollment.getEnrolledAt(),
                    enrollment.getCompletedAt()
            ));
        }

        return results;
    }
}
