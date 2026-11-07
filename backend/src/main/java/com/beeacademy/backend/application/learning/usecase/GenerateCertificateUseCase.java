package com.beeacademy.backend.application.learning.usecase;

import com.beeacademy.backend.application.learning.dto.CertificateResult;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.learning.Certificate;
import com.beeacademy.backend.domain.learning.CertificatePdfGeneratorPort;
import com.beeacademy.backend.domain.learning.CertificateRepositoryPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Use case Quản lý & Xuất file PDF Chứng chỉ số hoàn thành khóa học (UC20).
 */
@Service
public class GenerateCertificateUseCase {

    private final CertificateRepositoryPort certificateRepository;
    private final CertificatePdfGeneratorPort pdfGenerator;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    public GenerateCertificateUseCase(CertificateRepositoryPort certificateRepository,
                                      CertificatePdfGeneratorPort pdfGenerator) {
        this.certificateRepository = certificateRepository;
        this.pdfGenerator = pdfGenerator;
    }

    @Transactional(readOnly = true)
    public List<CertificateResult> getMyCertificates(UUID studentId) {
        return certificateRepository.findByStudentId(studentId).stream()
                .map(cert -> CertificateResult.from(cert, frontendUrl))
                .toList();
    }

    @Transactional(readOnly = true)
    public CertificateResult getCertificateDetail(UUID certificateId, UUID studentId) {
        Certificate cert = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new DomainException("CERTIFICATE_NOT_FOUND", "Không tìm thấy chứng chỉ"));

        if (!cert.getStudentId().equals(studentId)) {
            throw new DomainException("FORBIDDEN", "Bạn không có quyền truy cập chứng chỉ này");
        }

        return CertificateResult.from(cert, frontendUrl);
    }

    @Transactional(readOnly = true)
    public byte[] downloadPdf(UUID certificateId, UUID studentId) {
        Certificate cert = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new DomainException("CERTIFICATE_NOT_FOUND", "Không tìm thấy chứng chỉ"));

        if (!cert.getStudentId().equals(studentId)) {
            throw new DomainException("FORBIDDEN", "Bạn không có quyền tải chứng chỉ này");
        }

        return pdfGenerator.generateCertificatePdf(
                cert.getCertificateCode(),
                cert.getStudentName(),
                cert.getCourseTitle(),
                cert.getIssuedAt()
        );
    }

    @Transactional(readOnly = true)
    public CertificateResult verifyByCode(String certificateCode) {
        Certificate cert = certificateRepository.findByCertificateCode(certificateCode)
                .orElseThrow(() -> new DomainException("CERTIFICATE_NOT_FOUND", "Mã chứng chỉ không tồn tại hoặc không hợp lệ"));

        return CertificateResult.from(cert, frontendUrl);
    }
}
