package com.beeacademy.backend.application.learning.usecase;

import com.beeacademy.backend.application.course.dto.CourseDocumentDto;
import com.beeacademy.backend.application.learning.dto.LearningLessonResult;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.course.Chapter;
import com.beeacademy.backend.domain.course.Course;
import com.beeacademy.backend.domain.course.CourseRepositoryPort;
import com.beeacademy.backend.domain.course.Lesson;
import com.beeacademy.backend.domain.learning.EnrollmentRepositoryPort;
import com.beeacademy.backend.domain.learning.StudentVideoProgress;
import com.beeacademy.backend.domain.learning.StudentVideoProgressRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Use case Lấy thông tin bài học trong Không gian học tập (UC14).
 * Kiểm tra quyền truy cập: Học viên phải đã ghi danh khóa học hoặc bài học là bản xem thử miễn phí.
 */
@Service
public class GetLearningLessonUseCase {

    private final CourseRepositoryPort courseRepository;
    private final EnrollmentRepositoryPort enrollmentRepository;
    private final StudentVideoProgressRepositoryPort progressRepository;

    public GetLearningLessonUseCase(CourseRepositoryPort courseRepository,
                                  EnrollmentRepositoryPort enrollmentRepository,
                                  StudentVideoProgressRepositoryPort progressRepository) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.progressRepository = progressRepository;
    }

    @Transactional(readOnly = true)
    public LearningLessonResult execute(UUID studentId, UUID courseId, UUID lessonId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new DomainException("COURSE_NOT_FOUND", "Không tìm thấy khóa học"));

        // Tìm bài học trong cấu trúc cây chương mục
        Lesson targetLesson = null;
        UUID chapterId = null;
        for (Chapter ch : course.getChapters()) {
            for (Lesson l : ch.getLessons()) {
                if (l.getId().equals(lessonId)) {
                    targetLesson = l;
                    chapterId = ch.getId();
                    break;
                }
            }
            if (targetLesson != null) break;
        }

        if (targetLesson == null) {
            throw new DomainException("LESSON_NOT_FOUND", "Không tìm thấy bài học trong khóa học này");
        }

        final Lesson lesson = targetLesson;

        // Kiểm tra quyền: Đã ghi danh hoặc là bài học thử
        boolean isEnrolled = enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId);
        if (!isEnrolled && !lesson.isFreePreview()) {
            throw new DomainException("ACCESS_DENIED", "Bạn cần đăng ký khóa học để truy cập bài học này");
        }

        // Lấy tiến độ học của học viên
        Optional<StudentVideoProgress> progressOpt = progressRepository.findByStudentIdAndLessonId(studentId, lessonId);
        boolean isCompleted = progressOpt.map(StudentVideoProgress::isCompleted).orElse(false);
        int lastPos = progressOpt.map(StudentVideoProgress::getLastPositionSec).orElse(0);
        double watchPercent = progressOpt
                .map(p -> p.getWatchSegments().calculateWatchPercent(lesson.getDurationSec()).value().doubleValue())
                .orElse(0.0);

        List<CourseDocumentDto> docDtos = lesson.getDocuments() != null
                ? lesson.getDocuments().stream().map(CourseDocumentDto::from).toList()
                : List.of();

        return new LearningLessonResult(
                lesson.getId(),
                chapterId,
                lesson.getTitle(),
                lesson.getOrderIndex(),
                lesson.getVideoUrl(),
                lesson.getDurationSec(),
                lesson.isFreePreview(),
                isCompleted,
                lastPos,
                watchPercent,
                docDtos
        );
    }
}
