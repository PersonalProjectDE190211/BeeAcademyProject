package com.beeacademy.backend.domain.learning;

import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.learning.event.CertificateIssuedEvent;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * Domain Policy kiểm tra điều kiện cấp phát Chứng chỉ khóa học (UC20).
 * Quy tắc nghiệp vụ bắt buộc:
 * 1. Học viên phải hoàn thành 100% các bài học trong khóa học (Enrollment.isCompleted() == true).
 * 2. Học viên phải vượt qua (đạt điểm Pass) bài thi/kiểm tra cuối khóa (hasPassedFinalExam == true).
 */
public class CertificateEligibilityPolicy {

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Kiểm tra nhanh xem học viên có đủ điều kiện nhận chứng chỉ hay không.
     */
    public static boolean isEligible(Enrollment enrollment, boolean hasPassedFinalExam) {
        if (enrollment == null) return false;
        return enrollment.isCompleted() && hasPassedFinalExam;
    }

    /**
     * Thực hiện kiểm tra và cấp phát chứng chỉ chính thức.
     * Ném DomainException nếu chưa thỏa mãn điều kiện.
     */
    public static Certificate issueCertificate(Enrollment enrollment,
                                               String studentName,
                                               String courseTitle,
                                               boolean hasPassedFinalExam) {
        if (enrollment == null) {
            throw new DomainException("INVALID_ENROLLMENT", "Thông tin ghi danh không hợp lệ");
        }
        if (!enrollment.isCompleted()) {
            throw new DomainException("COURSE_NOT_COMPLETED",
                    "Học viên chưa hoàn thành 100% bài học của khóa học (Tiến độ hiện tại: " + enrollment.getProgressPercent().value() + "%)");
        }
        if (!hasPassedFinalExam) {
            throw new DomainException("EXAM_NOT_PASSED",
                    "Học viên chưa đạt điểm bài thi cuối khóa để được cấp chứng chỉ");
        }

        String code = generateCertificateCode();
        return Certificate.issue(
                code,
                enrollment.getStudentId(),
                enrollment.getCourseId(),
                studentName,
                courseTitle
        );
    }

    private static String generateCertificateCode() {
        int randomNum = 100000 + RANDOM.nextInt(900000);
        return "BEE-CERT-" + randomNum;
    }
}
