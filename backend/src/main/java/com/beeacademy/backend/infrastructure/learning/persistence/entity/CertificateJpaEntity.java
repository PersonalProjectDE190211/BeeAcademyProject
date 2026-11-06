package com.beeacademy.backend.infrastructure.learning.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA Entity ánh xạ bảng 'certificates' (Chứng chỉ hoàn thành khóa học).
 */
@Entity
@Table(name = "certificates")
public class CertificateJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "enrollment_id", nullable = false, unique = true)
    private UUID enrollmentId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(name = "serial_number", nullable = false, unique = true, length = 64)
    private String serialNumber;

    @Column(name = "student_name_snapshot", nullable = false, length = 150)
    private String studentNameSnapshot;

    @Column(name = "course_title_snapshot", nullable = false, length = 255)
    private String courseTitleSnapshot;

    @Column(name = "final_exam_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal finalExamScore;

    @Column(name = "pdf_url", length = 1000)
    private String pdfUrl;

    @Column(name = "verification_qr_url", length = 1000)
    private String verificationQrUrl;

    @Column(name = "revoked", nullable = false)
    private boolean revoked;

    @Column(name = "issued_at", nullable = false, updatable = false)
    private Instant issuedAt;

    public CertificateJpaEntity() {
    }

    public CertificateJpaEntity(UUID id,
                                UUID enrollmentId,
                                UUID studentId,
                                UUID courseId,
                                String serialNumber,
                                String studentNameSnapshot,
                                String courseTitleSnapshot,
                                BigDecimal finalExamScore,
                                String pdfUrl,
                                String verificationQrUrl,
                                boolean revoked,
                                Instant issuedAt) {
        this.id = id;
        this.enrollmentId = enrollmentId;
        this.studentId = studentId;
        this.courseId = courseId;
        this.serialNumber = serialNumber;
        this.studentNameSnapshot = studentNameSnapshot;
        this.courseTitleSnapshot = courseTitleSnapshot;
        this.finalExamScore = finalExamScore;
        this.pdfUrl = pdfUrl;
        this.verificationQrUrl = verificationQrUrl;
        this.revoked = revoked;
        this.issuedAt = issuedAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(UUID enrollmentId) { this.enrollmentId = enrollmentId; }
    public UUID getStudentId() { return studentId; }
    public void setStudentId(UUID studentId) { this.studentId = studentId; }
    public UUID getCourseId() { return courseId; }
    public void setCourseId(UUID courseId) { this.courseId = courseId; }
    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
    public String getStudentNameSnapshot() { return studentNameSnapshot; }
    public void setStudentNameSnapshot(String studentNameSnapshot) { this.studentNameSnapshot = studentNameSnapshot; }
    public String getCourseTitleSnapshot() { return courseTitleSnapshot; }
    public void setCourseTitleSnapshot(String courseTitleSnapshot) { this.courseTitleSnapshot = courseTitleSnapshot; }
    public BigDecimal getFinalExamScore() { return finalExamScore; }
    public void setFinalExamScore(BigDecimal finalExamScore) { this.finalExamScore = finalExamScore; }
    public String getPdfUrl() { return pdfUrl; }
    public void setPdfUrl(String pdfUrl) { this.pdfUrl = pdfUrl; }
    public String getVerificationQrUrl() { return verificationQrUrl; }
    public void setVerificationQrUrl(String verificationQrUrl) { this.verificationQrUrl = verificationQrUrl; }
    public boolean isRevoked() { return revoked; }
    public void setRevoked(boolean revoked) { this.revoked = revoked; }
    public Instant getIssuedAt() { return issuedAt; }
    public void setIssuedAt(Instant issuedAt) { this.issuedAt = issuedAt; }
}
