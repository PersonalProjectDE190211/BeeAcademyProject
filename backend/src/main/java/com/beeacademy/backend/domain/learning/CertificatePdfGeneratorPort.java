package com.beeacademy.backend.domain.learning;

import java.time.Instant;

/**
 * Domain Port xuất file PDF Chứng chỉ số hoàn thành khóa học kèm mã QR xác minh.
 */
public interface CertificatePdfGeneratorPort {

    byte[] generateCertificatePdf(String certificateCode, String studentName, String courseTitle, Instant issuedAt);
}
