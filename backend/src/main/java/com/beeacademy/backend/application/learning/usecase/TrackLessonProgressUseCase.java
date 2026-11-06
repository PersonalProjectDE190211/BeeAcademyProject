package com.beeacademy.backend.application.learning.usecase;

import com.beeacademy.backend.application.learning.dto.LessonProgressResult;
import com.beeacademy.backend.application.learning.dto.TrackProgressCommand;
import com.beeacademy.backend.domain.auth.Profile;
import com.beeacademy.backend.domain.auth.ProfileRepositoryPort;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.course.Chapter;
import com.beeacademy.backend.domain.course.Course;
import com.beeacademy.backend.domain.course.CourseRepositoryPort;
import com.beeacademy.backend.domain.course.Lesson;
import com.beeacademy.backend.domain.learning.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.Year;
import java.util.Optional;
import java.util.UUID;

/**
 * Use case Theo dõi tiến độ xem video bài học & Thuật toán chống tua (UC14, UC15).
 * - Gộp các khoảng xem hợp lệ bằng cấu trúc WatchSegments.
 * - Đánh dấu hoàn thành bài học khi xem >= 80% thời lượng.
 * - Tự động cập nhật % tiến độ tổng thể khóa học và tự động cấp chứng chỉ khi đạt 100%.
 */
@Service
public class TrackLessonProgressUseCase {

    private final CourseRepositoryPort courseRepository;
    private final EnrollmentRepositoryPort enrollmentRepository;
    private final StudentVideoProgressRepositoryPort progressRepository;
    private final CertificateRepositoryPort certificateRepository;
    private final ProfileRepositoryPort profileRepository;

    public TrackLessonProgressUseCase(CourseRepositoryPort courseRepository,
                                     EnrollmentRepositoryPort enrollmentRepository,
                                     StudentVideoProgressRepositoryPort progressRepository,
                                     CertificateRepositoryPort certificateRepository,
                                     ProfileRepositoryPort profileRepository) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.progressRepository = progressRepository;
        this.certificateRepository = certificateRepository;
        this.profileRepository = profileRepository;
    }

    @Transactional
    public LessonProgressResult execute(TrackProgressCommand command) {
        if (command == null) {
            throw new DomainException("INVALID_COMMAND", "Dữ liệu tiến độ không được rỗng");
        }

        Course course = courseRepository.findById(command.courseId())
                .orElseThrow(() -> new DomainException("COURSE_NOT_FOUND", "Không tìm thấy khóa học"));

        // Tìm bài học trong khóa
        Lesson lesson = null;
        for (Chapter ch : course.getChapters()) {
            for (Lesson l : ch.getLessons()) {
                if (l.getId().equals(command.lessonId())) {
                    lesson = l;
                    break;
                }
            }
            if (lesson != null) break;
        }

        if (lesson == null) {
            throw new DomainException("LESSON_NOT_FOUND", "Không tìm thấy bài học");
        }

        // Tìm hoặc tạo tiến độ xem bài học
        StudentVideoProgress progress = progressRepository.findByStudentIdAndLessonId(command.studentId(), command.lessonId())
                .orElseGet(() -> StudentVideoProgress.create(command.studentId(), command.courseId(), command.lessonId()));

        Instant now = Instant.now();
        progress.recordWatch(command.startSec(), command.endSec(), command.currentPositionSec(), lesson.getDurationSec(), now);
        StudentVideoProgress savedProgress = progressRepository.save(progress);

        // Cập nhật tiến độ tổng thể khóa học trong Enrollment
        double courseProgressPercent = 0.0;
        boolean courseCompleted = false;

        Optional<Enrollment> enrollmentOpt = enrollmentRepository.findByStudentIdAndCourseId(command.studentId(), command.courseId());
        if (enrollmentOpt.isPresent()) {
            Enrollment enrollment = enrollmentOpt.get();
            long completedLessons = progressRepository.countCompletedLessons(command.studentId(), command.courseId());
            enrollment.updateProgress((int) completedLessons, course.getTotalLessons(), now);
            Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

            courseProgressPercent = savedEnrollment.getProgressPercent() != null
                    ? savedEnrollment.getProgressPercent().value().doubleValue()
                    : 0.0;
            courseCompleted = savedEnrollment.isCompleted();

            // Nếu khóa học đã hoàn thành 100% và chưa có chứng chỉ -> Tự động cấp chứng chỉ số (UC20)
            if (courseCompleted && !certificateRepository.existsByStudentIdAndCourseId(command.studentId(), command.courseId())) {
                issueCertificateInternal(command.studentId(), course);
            }
        }

        int watchedSecs = savedProgress.getWatchSegments().calculateTotalUniqueSeconds();
        double watchPercent = savedProgress.getWatchSegments()
                .calculateWatchPercent(lesson.getDurationSec())
                .value()
                .doubleValue();

        return new LessonProgressResult(
                savedProgress.getLessonId(),
                savedProgress.getCourseId(),
                watchedSecs,
                savedProgress.getLastPositionSec(),
                watchPercent,
                savedProgress.isCompleted(),
                savedProgress.getCompletedAt(),
                courseProgressPercent,
                courseCompleted
        );
    }

    private void issueCertificateInternal(UUID studentId, Course course) {
        String studentName = profileRepository.findById(studentId)
                .map(Profile::getFullName)
                .orElse("Học viên BeeAcademy");

        String serialNumber = "CERT-" + Year.now().getValue() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Certificate cert = Certificate.issue(serialNumber, studentId, course.getId(), studentName, course.getTitle());
        certificateRepository.save(cert);
    }
}
