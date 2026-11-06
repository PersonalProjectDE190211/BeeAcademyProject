package com.beeacademy.backend.application.course.usecase;

import com.beeacademy.backend.application.course.dto.CourseDetailResult;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.course.Course;
import com.beeacademy.backend.domain.course.CourseRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case Nộp khóa học để Admin kiểm duyệt (UC30, UC41).
 */
@Service
public class SubmitCourseForReviewUseCase {

    private final CourseRepositoryPort courseRepository;

    public SubmitCourseForReviewUseCase(CourseRepositoryPort courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Transactional
    public CourseDetailResult execute(UUID courseId, UUID teacherId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new DomainException("COURSE_NOT_FOUND", "Không tìm thấy khóa học với ID: " + courseId));

        if (!course.getTeacherId().equals(teacherId)) {
            throw new DomainException("FORBIDDEN", "Bạn không có quyền nộp duyệt khóa học này");
        }

        course.submitForReview();
        Course saved = courseRepository.save(course);
        return CourseDetailResult.from(saved, true);
    }
}

