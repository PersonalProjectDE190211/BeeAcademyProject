package com.beeacademy.backend.application.family.usecase;

import com.beeacademy.backend.application.family.dto.StudentCourseProgressItem;
import com.beeacademy.backend.application.family.dto.StudentProgressReportResult;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.course.Course;
import com.beeacademy.backend.domain.course.CourseRepositoryPort;
import com.beeacademy.backend.domain.family.ParentProgressAccessAudit;
import com.beeacademy.backend.domain.family.ParentProgressAccessAuditRepositoryPort;
import com.beeacademy.backend.domain.family.ParentStudentLink;
import com.beeacademy.backend.domain.family.ParentStudentLinkRepositoryPort;
import com.beeacademy.backend.domain.learning.Enrollment;
import com.beeacademy.backend.domain.learning.EnrollmentRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ParentViewStudentProgressUseCase {

    private final ParentStudentLinkRepositoryPort linkRepository;
    private final ParentProgressAccessAuditRepositoryPort auditRepository;
    private final EnrollmentRepositoryPort enrollmentRepository;
    private final CourseRepositoryPort courseRepository;

    public ParentViewStudentProgressUseCase(
            ParentStudentLinkRepositoryPort linkRepository,
            ParentProgressAccessAuditRepositoryPort auditRepository,
            EnrollmentRepositoryPort enrollmentRepository,
            CourseRepositoryPort courseRepository
    ) {
        this.linkRepository = linkRepository;
        this.auditRepository = auditRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional
    public StudentProgressReportResult execute(UUID parentId, UUID studentId) {
        ParentStudentLink link = linkRepository.findByParentIdAndStudentId(parentId, studentId)
                .orElseThrow(() -> new DomainException("LINK_NOT_FOUND", "Không tìm thấy mối liên kết giữa Phụ huynh và Học sinh"));

        if (!link.isDataAccessibleByParent(parentId)) {
            throw new DomainException("ACCESS_DENIED", "Phụ huynh chưa được cấp quyền xem dữ liệu học tập của học sinh này (liên kết chưa kích hoạt hoặc đã bị thu hồi)");
        }

        // Ghi vết kiểm toán tuân thủ quyền riêng tư (Privacy Audit Log)
        auditRepository.save(ParentProgressAccessAudit.record(parentId, studentId, "VIEW_LEARNING_PROGRESS"));

        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        List<StudentCourseProgressItem> courseItems = new ArrayList<>();
        int completedCount = 0;

        for (Enrollment enrollment : enrollments) {
            String courseTitle = "Khóa học";
            Course course = courseRepository.findById(enrollment.getCourseId()).orElse(null);
            if (course != null) {
                courseTitle = course.getTitle();
            }

            if (enrollment.isCompleted()) {
                completedCount++;
            }

            courseItems.add(new StudentCourseProgressItem(
                    enrollment.getCourseId(),
                    courseTitle,
                    enrollment.getProgressPercent().value().doubleValue(),
                    enrollment.isCompleted(),
                    enrollment.getLastAccessedAt()
            ));
        }

        return new StudentProgressReportResult(
                studentId,
                link.getStudentEmail(),
                enrollments.size(),
                completedCount,
                courseItems
        );
    }
}

