package com.beeacademy.backend.application.learning.dto;

import com.beeacademy.backend.domain.learning.Certificate;

import java.time.Instant;
import java.util.UUID;

public record CertificateResult(
        UUID id,
        String certificateCode,
        UUID studentId,
        UUID courseId,
        String studentName,
        String courseTitle,
        Instant issuedAt,
        String pdfUrl,
        String verifyUrl
) {
    public static CertificateResult from(Certificate cert, String frontendUrl) {
        String base = (frontendUrl != null && !frontendUrl.isBlank()) ? frontendUrl : "http://localhost:3000";
        return new CertificateResult(
                cert.getId(),
                cert.getCertificateCode(),
                cert.getStudentId(),
                cert.getCourseId(),
                cert.getStudentName(),
                cert.getCourseTitle(),
                cert.getIssuedAt(),
                cert.getPdfUrl(),
                base + "/verify-certificate/" + cert.getCertificateCode()
        );
    }
}
