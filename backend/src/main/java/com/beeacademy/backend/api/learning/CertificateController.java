package com.beeacademy.backend.api.learning;

import com.beeacademy.backend.api.common.ApiResponse;
import com.beeacademy.backend.application.learning.dto.CertificateResult;
import com.beeacademy.backend.application.learning.usecase.GenerateCertificateUseCase;
import com.beeacademy.backend.platform.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller Quản lý, Tải về và Xác thực Chứng chỉ số (UC20).
 */
@Tag(name = "9. Chứng chỉ số (Certificates)", description = "Các API xem danh sách chứng chỉ, tải file PDF chuẩn Landscape A4 và tra cứu xác thực công khai")
@RestController
@RequestMapping("/api/certificates")
public class CertificateController {

    private final GenerateCertificateUseCase generateCertificateUseCase;

    public CertificateController(GenerateCertificateUseCase generateCertificateUseCase) {
        this.generateCertificateUseCase = generateCertificateUseCase;
    }

    /**
     * Lấy danh sách chứng chỉ của học viên đang đăng nhập.
     */
    @Operation(summary = "Chứng chỉ của tôi")
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<CertificateResult>>> getMyCertificates() {
        UUID studentId = SecurityUtils.requireCurrentUserId();
        List<CertificateResult> certificates = generateCertificateUseCase.getMyCertificates(studentId);
        return ResponseEntity.ok(ApiResponse.success(certificates));
    }

    /**
     * Tải file PDF chứng chỉ số (Landscape A4, hoa văn viền đôi, mã QR xác minh).
     */
    @Operation(summary = "Tải file PDF chứng chỉ số")
    @GetMapping("/{id}/download")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> downloadCertificatePdf(@PathVariable UUID id) {
        UUID studentId = SecurityUtils.requireCurrentUserId();
        byte[] pdfBytes = generateCertificateUseCase.downloadPdf(id, studentId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"certificate-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    /**
     * Tra cứu và xác thực tính hợp lệ của chứng chỉ công khai (Không yêu cầu đăng nhập).
     */
    @Operation(summary = "Tra cứu xác minh chứng chỉ (Công khai)", description = "Dùng khi nhà tuyển dụng hoặc phụ huynh quét mã QR để kiểm tra tính chính xác của chứng chỉ")
    @GetMapping("/public/verify/{code}")
    public ResponseEntity<ApiResponse<CertificateResult>> verifyCertificate(@PathVariable String code) {
        CertificateResult result = generateCertificateUseCase.verifyByCode(code);
        return ResponseEntity.ok(ApiResponse.success("Chứng chỉ hợp lệ và đã được xác thực bởi BeeAcademy", result));
    }
}
