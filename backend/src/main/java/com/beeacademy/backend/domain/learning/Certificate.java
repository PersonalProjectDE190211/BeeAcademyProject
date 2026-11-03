package com.beeacademy.backend.domain.learning;

import com.beeacademy.backend.domain.common.AbstractAggregateRoot;
import com.beeacademy.backend.domain.common.DomainException;

import java.time.Instant;
import java.util.UUID;

/**
 * Aggregate Root đại diện cho Chứng chỉ hoàn thành khóa học của học viên (UC20).
 * Được cấp phát khi học viên hoàn thành 100% nội dung và vượt qua bài thi cuối khóa.
 */
public class Certificate extends AbstractAggregateRoot {

    private final UUID id;
    private final String certificateCode;
    private final UUID studentId;
    private final UUID courseId;
    private final String studentName;
    private final String courseTitle;
    private final Instant issuedAt;
    private String pdfUrl;

    public Certificate(UUID id,
                       String certificateCode,
                       UUID studentId,
                       UUID courseId,
                       String studentName,
                       String courseTitle,
                       Instant issuedAt,
                       String pdfUrl) {
        if (id == null) throw new DomainException("INVALID_ID", "ID không được null");
        if (certificateCode == null || certificateCode.isBlank()) throw new DomainException("INVALID_CODE", "Mã chứng chỉ không được để trống");
        if (studentId == null) throw new DomainException("INVALID_STUDENT_ID", "Student ID không được null");
        if (courseId == null) throw new DomainException("INVALID_COURSE_ID", "Course ID không được null");
        if (studentName == null || studentName.isBlank()) throw new DomainException("INVALID_STUDENT_NAME", "Tên học viên không được để trống");
        if (courseTitle == null || courseTitle.isBlank()) throw new DomainException("INVALID_COURSE_TITLE", "Tên khóa học không được để trống");

        this.id = id;
        this.certificateCode = certificateCode.trim();
        this.studentId = studentId;
        this.courseId = courseId;
        this.studentName = studentName.trim();
        this.courseTitle = courseTitle.trim();
        this.issuedAt = issuedAt != null ? issuedAt : Instant.now();
        this.pdfUrl = pdfUrl != null ? pdfUrl.trim() : "";
    }

    public static Certificate issue(String certificateCode,
                                    UUID studentId,
                                    UUID courseId,
                                    String studentName,
                                    String courseTitle) {
        Certificate cert = new Certificate(
                UUID.randomUUID(),
                certificateCode,
                studentId,
                courseId,
                studentName,
                courseTitle,
                Instant.now(),
                ""
        );
        cert.registerDomainEvent(com.beeacademy.backend.domain.learning.event.CertificateIssuedEvent.of(
                cert.getId(),
                cert.getStudentId(),
                cert.getCourseId(),
                cert.getCertificateCode()
        ));
        return cert;
    }

    public void updatePdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl != null ? pdfUrl.trim() : "";
    }

    public UUID getId() { return id; }
    public String getCertificateCode() { return certificateCode; }
    public UUID getStudentId() { return studentId; }
    public UUID getCourseId() { return courseId; }
    public String getStudentName() { return studentName; }
    public String getCourseTitle() { return courseTitle; }
    public Instant getIssuedAt() { return issuedAt; }
    public String getPdfUrl() { return pdfUrl; }
}
