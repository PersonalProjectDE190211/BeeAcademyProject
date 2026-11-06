package com.beeacademy.backend.application.learning;

import com.beeacademy.backend.application.learning.dto.LessonProgressResult;
import com.beeacademy.backend.application.learning.dto.TrackProgressCommand;
import com.beeacademy.backend.application.learning.usecase.TrackLessonProgressUseCase;
import com.beeacademy.backend.domain.auth.Profile;
import com.beeacademy.backend.domain.auth.ProfileRepositoryPort;
import com.beeacademy.backend.domain.course.Chapter;
import com.beeacademy.backend.domain.course.Course;
import com.beeacademy.backend.domain.course.CourseRepositoryPort;
import com.beeacademy.backend.domain.learning.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DisplayName("TrackLessonProgressUseCase Unit Tests (UC14, UC15, UC20)")
class TrackLessonProgressUseCaseTest {

    private CourseRepositoryPort courseRepository;
    private EnrollmentRepositoryPort enrollmentRepository;
    private StudentVideoProgressRepositoryPort progressRepository;
    private CertificateRepositoryPort certificateRepository;
    private ProfileRepositoryPort profileRepository;
    private TrackLessonProgressUseCase useCase;

    @BeforeEach
    void setUp() {
        courseRepository = Mockito.mock(CourseRepositoryPort.class);
        enrollmentRepository = Mockito.mock(EnrollmentRepositoryPort.class);
        progressRepository = Mockito.mock(StudentVideoProgressRepositoryPort.class);
        certificateRepository = Mockito.mock(CertificateRepositoryPort.class);
        profileRepository = Mockito.mock(ProfileRepositoryPort.class);

        useCase = new TrackLessonProgressUseCase(
                courseRepository,
                enrollmentRepository,
                progressRepository,
                certificateRepository,
                profileRepository
        );
    }

    @Test
    @DisplayName("Xem video dưới 80% thời lượng -> chưa đánh dấu hoàn thành bài học")
    void trackProgress_Under80Percent_NotCompleted() {
        UUID studentId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();

        Course course = Course.create(UUID.randomUUID(), UUID.randomUUID(), "Vật Lý 12", "Dao động", null, null, List.of(12));
        Chapter ch = course.addChapter("Chương 1", "");
        // Video dài 1000 giây (16.6 phút)
        var lesson = course.addLesson(ch.getId(), "Bài 1", "https://video.mp4", 1000, true);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        StudentVideoProgress progress = StudentVideoProgress.create(studentId, courseId, lesson.getId());
        when(progressRepository.findByStudentIdAndLessonId(studentId, lesson.getId())).thenReturn(Optional.of(progress));
        when(progressRepository.save(any(StudentVideoProgress.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Enrollment enrollment = Enrollment.create(studentId, courseId, null);
        when(enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)).thenReturn(Optional.of(enrollment));
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Học viên xem từ giây 0 đến 500 (50% < 80%)
        TrackProgressCommand cmd = new TrackProgressCommand(studentId, courseId, lesson.getId(), 0, 500, 500);
        LessonProgressResult result = useCase.execute(cmd);

        assertThat(result.isCompleted()).isFalse();
        assertThat(result.watchedSeconds()).isEqualTo(500);
        assertThat(result.watchPercent()).isEqualTo(50.0);
        assertThat(result.courseCompleted()).isFalse();
    }

    @Test
    @DisplayName("Xem video đạt >= 80% thời lượng -> hoàn thành bài học và tự động cấp chứng chỉ khi 100% khóa học")
    void trackProgress_Over80Percent_CompletesAndIssuesCertificate() {
        UUID studentId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();

        Course course = Course.create(UUID.randomUUID(), UUID.randomUUID(), "Toán 12", "Tích phân", null, null, List.of(12));
        Chapter ch = course.addChapter("Chương 1", "");
        // Khóa học chỉ có 1 bài học dài 600 giây
        var lesson = course.addLesson(ch.getId(), "Bài 1", "https://video.mp4", 600, true);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        StudentVideoProgress progress = StudentVideoProgress.create(studentId, courseId, lesson.getId());
        when(progressRepository.findByStudentIdAndLessonId(studentId, lesson.getId())).thenReturn(Optional.of(progress));
        when(progressRepository.save(any(StudentVideoProgress.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Enrollment enrollment = Enrollment.create(studentId, courseId, null);
        when(enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)).thenReturn(Optional.of(enrollment));
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Học viên xem từ 0 đến 500s (83.3% >= 80%) -> đạt hoàn thành
        when(progressRepository.countCompletedLessons(studentId, courseId)).thenReturn(1L);

        Profile studentProfile = Mockito.mock(Profile.class);
        when(studentProfile.getFullName()).thenReturn("Võ Văn Thành Đạt");
        when(profileRepository.findById(studentId)).thenReturn(Optional.of(studentProfile));

        TrackProgressCommand cmd = new TrackProgressCommand(studentId, courseId, lesson.getId(), 0, 500, 500);
        LessonProgressResult result = useCase.execute(cmd);

        assertThat(result.isCompleted()).isTrue();
        assertThat(result.courseCompleted()).isTrue();
        assertThat(result.courseProgressPercent()).isEqualTo(100.0);

        // Xác nhận hàm lưu chứng chỉ đã được gọi
        Mockito.verify(certificateRepository).save(any(Certificate.class));
    }
}
