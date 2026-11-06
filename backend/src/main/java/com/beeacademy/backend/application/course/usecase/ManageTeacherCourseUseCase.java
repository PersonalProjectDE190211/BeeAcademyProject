package com.beeacademy.backend.application.course.usecase;

import com.beeacademy.backend.application.course.dto.*;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.common.MoneyVnd;
import com.beeacademy.backend.domain.course.Course;
import com.beeacademy.backend.domain.course.CourseRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Use case Quản lý khóa học dành cho Giáo viên (UC30, UC31):
 * - Tạo khóa học mới (DRAFT).
 * - Cập nhật thông tin cơ bản và học phí.
 * - Thêm / xóa chương học và bài giảng (tự động cập nhật tổng chương, bài và thời lượng).
 * - Xem danh sách khóa học của giáo viên.
 */
@Service
public class ManageTeacherCourseUseCase {

    private final CourseRepositoryPort courseRepository;

    public ManageTeacherCourseUseCase(CourseRepositoryPort courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Transactional
    public CourseDetailResult createCourse(CreateCourseCommand command) {
        if (command == null) {
            throw new DomainException("INVALID_COMMAND", "Dữ liệu tạo khóa học không được rỗng");
        }

        Course course = Course.create(
                command.teacherId(),
                command.categoryId(),
                command.title(),
                command.description(),
                command.thumbnailUrl(),
                MoneyVnd.of(command.priceVnd()),
                command.grades()
        );

        Course saved = courseRepository.save(course);
        return CourseDetailResult.from(saved, true);
    }

    @Transactional
    public CourseDetailResult updateCourse(UpdateCourseCommand command) {
        if (command == null) {
            throw new DomainException("INVALID_COMMAND", "Dữ liệu cập nhật không được rỗng");
        }

        Course course = findAndVerifyOwnership(command.courseId(), command.teacherId());

        course.updateBasicInfo(
                command.title(),
                command.description(),
                command.thumbnailUrl(),
                command.categoryId(),
                command.grades()
        );
        course.updatePrice(MoneyVnd.of(command.priceVnd()));

        Course saved = courseRepository.save(course);
        return CourseDetailResult.from(saved, true);
    }

    @Transactional
    public CourseDetailResult addChapter(AddChapterCommand command) {
        if (command == null) {
            throw new DomainException("INVALID_COMMAND", "Dữ liệu chương không được rỗng");
        }

        Course course = findAndVerifyOwnership(command.courseId(), command.teacherId());
        course.addChapter(command.title(), command.description());

        Course saved = courseRepository.save(course);
        return CourseDetailResult.from(saved, true);
    }

    @Transactional
    public CourseDetailResult deleteChapter(UUID courseId, UUID teacherId, UUID chapterId) {
        Course course = findAndVerifyOwnership(courseId, teacherId);
        course.removeChapter(chapterId);

        Course saved = courseRepository.save(course);
        return CourseDetailResult.from(saved, true);
    }

    @Transactional
    public CourseDetailResult addLesson(AddLessonCommand command) {
        if (command == null) {
            throw new DomainException("INVALID_COMMAND", "Dữ liệu bài giảng không được rỗng");
        }

        Course course = findAndVerifyOwnership(command.courseId(), command.teacherId());
        course.addLesson(
                command.chapterId(),
                command.title(),
                command.videoUrl(),
                command.durationSec(),
                command.isFreePreview()
        );

        Course saved = courseRepository.save(course);
        return CourseDetailResult.from(saved, true);
    }

    @Transactional
    public CourseDetailResult deleteLesson(UUID courseId, UUID teacherId, UUID chapterId, UUID lessonId) {
        Course course = findAndVerifyOwnership(courseId, teacherId);
        course.removeLesson(chapterId, lessonId);

        Course saved = courseRepository.save(course);
        return CourseDetailResult.from(saved, true);
    }

    @Transactional(readOnly = true)
    public List<CourseSummaryResult> getTeacherCourses(UUID teacherId) {
        return courseRepository.findByTeacherId(teacherId).stream()
                .map(CourseSummaryResult::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CourseDetailResult getTeacherCourseDetail(UUID courseId, UUID teacherId) {
        Course course = findAndVerifyOwnership(courseId, teacherId);
        return CourseDetailResult.from(course, true);
    }

    private Course findAndVerifyOwnership(UUID courseId, UUID teacherId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new DomainException("COURSE_NOT_FOUND", "Không tìm thấy khóa học với ID: " + courseId));

        if (!course.getTeacherId().equals(teacherId)) {
            throw new DomainException("FORBIDDEN", "Bạn không có quyền chỉnh sửa khóa học của giáo viên khác");
        }
        return course;
    }
}

