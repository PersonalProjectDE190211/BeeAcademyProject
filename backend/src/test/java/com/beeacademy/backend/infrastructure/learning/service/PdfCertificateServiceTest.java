package com.beeacademy.backend.infrastructure.learning.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PdfCertificateService Tests")
class PdfCertificateServiceTest {

    private PdfCertificateService pdfCertificateService;

    @BeforeEach
    void setUp() {
        pdfCertificateService = new PdfCertificateService();
        ReflectionTestUtils.setField(pdfCertificateService, "frontendUrl", "http://localhost:3000");
    }

    @Test
    @DisplayName("Tạo file PDF chứng chỉ thành công với mã header chuẩn %PDF-")
    void generateCertificatePdf_Success() {
        String certCode = "CERT-2026-TEST01";
        String studentName = "Nguyễn Văn A";
        String courseTitle = "Lập trình Java 21 nâng cao";
        Instant issuedAt = Instant.now();

        byte[] pdfBytes = pdfCertificateService.generateCertificatePdf(certCode, studentName, courseTitle, issuedAt);

        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes.length).isGreaterThan(1000);

        // Kiểm tra magic number chuẩn của định dạng PDF: %PDF-
        String header = new String(pdfBytes, 0, 5, StandardCharsets.US_ASCII);
        assertThat(header).isEqualTo("%PDF-");
    }
}
