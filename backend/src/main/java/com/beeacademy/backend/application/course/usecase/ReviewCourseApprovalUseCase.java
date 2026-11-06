package com.beeacademy.backend.application.course.usecase;

import com.beeacademy.backend.application.course.dto.CourseDetailResult;
import com.beeacademy.backend.application.course.dto.CourseSummaryResult;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.course.Course;
import com.beeacademy.backend.domain.course.CourseRepositoryPort;
import com.beeacademy.backend.domain.course.CourseStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Use case Phê duyệt khóa học dành cho Quản trị viên (UC41):
 * - Phê duyệt xuất bản (PUBLISHED).
 * - Từ chối phê duyệt (REJECTED).
 * - Yêu cầu giáo viên sửa đổi (NEEDS_REVISION).
 */
@Service
public class ReviewCourseApprovalUseCase {

    private final CourseRepositoryPort courseRepository;

    public ReviewCourseApprovalUseCase(CourseRepositoryPort courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Transactional(readOnly = true)
    public List<CourseSummaryResult> getPendingCourses() {
        return courseRepository.findByStatus(CourseStatus.PENDING_REVIEW).stream()
                .map(CourseSummaryResult::from)
                .toList();
    }

    @Transactional
    public CourseDetailResult approve(UUID courseId, UUID adminId, String comment) {
        Course course = findCourse(courseId);
        course.approve(adminId, comment);
        Course saved = courseRepository.save(course);
        return CourseDetailResult.from(saved, true);
    }

    @Transactional
    public CourseDetailResult reject(UUID courseId, UUID adminId, String reason) {
        Course course = findCourse(courseId);
        course.reject(adminId, reason);
        Course saved = courseRepository.save(course);
        return CourseDetailResult.from(saved, true);
    }

    @Transactional
    public CourseDetailResult requestRevision(UUID courseId, UUID adminId, String feedback) {
        Course course = findCourse(courseId);
        course.requestRevision(adminId, feedback);
        Course saved = courseRepository.save(course);
        return CourseDetailResult.from(saved, true);
    }

    private Course findCourse(UUID courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new DomainException("COURSE_NOT_FOUND", "Không tìm thấy khóa học với ID: " + courseId));
    }
}

