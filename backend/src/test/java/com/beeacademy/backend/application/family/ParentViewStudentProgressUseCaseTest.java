package com.beeacademy.backend.application.family;

import com.beeacademy.backend.application.family.dto.StudentProgressReportResult;
import com.beeacademy.backend.application.family.usecase.ParentViewStudentProgressUseCase;
import com.beeacademy.backend.domain.common.CourseSlug;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.common.MoneyVnd;
import com.beeacademy.backend.domain.course.Course;
import com.beeacademy.backend.domain.course.CourseRepositoryPort;
import com.beeacademy.backend.domain.course.CourseStatus;
import com.beeacademy.backend.domain.family.ParentProgressAccessAudit;
import com.beeacademy.backend.domain.family.ParentProgressAccessAuditRepositoryPort;
import com.beeacademy.backend.domain.family.ParentStudentLink;
import com.beeacademy.backend.domain.family.ParentStudentLinkRepositoryPort;
import com.beeacademy.backend.domain.learning.Enrollment;
import com.beeacademy.backend.domain.learning.EnrollmentRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParentViewStudentProgressUseCaseTest {

    @Mock
    private ParentStudentLinkRepositoryPort linkRepository;

    @Mock
    private ParentProgressAccessAuditRepositoryPort auditRepository;

    @Mock
    private EnrollmentRepositoryPort enrollmentRepository;

    @Mock
    private CourseRepositoryPort courseRepository;

    private ParentViewStudentProgressUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ParentViewStudentProgressUseCase(
                linkRepository, auditRepository, enrollmentRepository, courseRepository
        );
    }

    @Test
    @DisplayName("Phụ huynh xem tiến độ học tập thành công và tự động ghi vết kiểm toán quyền riêng tư")
    void viewStudentProgress_Success() {
        UUID parentId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();

        ParentStudentLink link = ParentStudentLink.invite(parentId, "student@bee.edu.vn", "TK1234", 72);
        link.accept(studentId, null);

        Enrollment enrollment = Enrollment.create(studentId, courseId, UUID.randomUUID());
        enrollment.updateProgress(5, 5, Instant.now()); // 100%

        Course course = new Course(
                courseId, UUID.randomUUID(), UUID.randomUUID(), "Toán Hình 10", CourseSlug.fromTitle("Toán Hình 10"),
                "", "", MoneyVnd.of(100000), CourseStatus.PUBLISHED, List.of(10), 1, 5, 3600,
                BigDecimal.ZERO, 0, null, Instant.now(), Instant.now(), Collections.emptyList(), Collections.emptyList()
        );

        when(linkRepository.findByParentIdAndStudentId(parentId, studentId)).thenReturn(Optional.of(link));
        when(enrollmentRepository.findByStudentId(studentId)).thenReturn(List.of(enrollment));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        StudentProgressReportResult result = useCase.execute(parentId, studentId);

        assertThat(result).isNotNull();
        assertThat(result.enrolledCoursesCount()).isEqualTo(1);
        assertThat(result.completedCoursesCount()).isEqualTo(1);
        assertThat(result.courses()).hasSize(1);
        assertThat(result.courses().get(0).courseTitle()).isEqualTo("Toán Hình 10");
        assertThat(result.courses().get(0).progressPercent()).isEqualTo(100.0);

        verify(auditRepository).save(any(ParentProgressAccessAudit.class));
    }

    @Test
    @DisplayName("Ném lỗi khi liên kết chưa được chấp thuận (chưa cấp quyền xem dữ liệu)")
    void viewStudentProgress_PendingLink_ThrowsException() {
        UUID parentId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();

        ParentStudentLink link = ParentStudentLink.invite(parentId, "student@bee.edu.vn", "TK9999", 72);
        // Chưa accept

        when(linkRepository.findByParentIdAndStudentId(parentId, studentId)).thenReturn(Optional.of(link));

        assertThatThrownBy(() -> useCase.execute(parentId, studentId))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Phụ huynh chưa được cấp quyền xem dữ liệu học tập");
    }
}

