package com.beeacademy.backend.infrastructure.learning.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA Entity ánh xạ bảng 'enrollments' (Ghi danh và Tiến độ tổng thể).
 */
@Entity
@Table(name = "enrollments")
public class EnrollmentJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(name = "completed_lessons", nullable = false)
    private int completedLessons;

    @Column(name = "total_lessons", nullable = false)
    private int totalLessons;

    @Column(name = "progress_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal progressPercent;

    @Column(name = "final_exam_passed", nullable = false)
    private boolean finalExamPassed;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "enrolled_at", nullable = false, updatable = false)
    private Instant enrolledAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public EnrollmentJpaEntity() {
    }

    public EnrollmentJpaEntity(UUID id,
                               UUID studentId,
                               UUID courseId,
                               int completedLessons,
                               int totalLessons,
                               BigDecimal progressPercent,
                               boolean finalExamPassed,
                               Instant completedAt,
                               Instant enrolledAt,
                               Instant updatedAt) {
        this.id = id;
        this.studentId = studentId;
        this.courseId = courseId;
        this.completedLessons = completedLessons;
        this.totalLessons = totalLessons;
        this.progressPercent = progressPercent;
        this.finalExamPassed = finalExamPassed;
        this.completedAt = completedAt;
        this.enrolledAt = enrolledAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getStudentId() { return studentId; }
    public void setStudentId(UUID studentId) { this.studentId = studentId; }
    public UUID getCourseId() { return courseId; }
    public void setCourseId(UUID courseId) { this.courseId = courseId; }
    public int getCompletedLessons() { return completedLessons; }
    public void setCompletedLessons(int completedLessons) { this.completedLessons = completedLessons; }
    public int getTotalLessons() { return totalLessons; }
    public void setTotalLessons(int totalLessons) { this.totalLessons = totalLessons; }
    public BigDecimal getProgressPercent() { return progressPercent; }
    public void setProgressPercent(BigDecimal progressPercent) { this.progressPercent = progressPercent; }
    public boolean isFinalExamPassed() { return finalExamPassed; }
    public void setFinalExamPassed(boolean finalExamPassed) { this.finalExamPassed = finalExamPassed; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public Instant getEnrolledAt() { return enrolledAt; }
    public void setEnrolledAt(Instant enrolledAt) { this.enrolledAt = enrolledAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
