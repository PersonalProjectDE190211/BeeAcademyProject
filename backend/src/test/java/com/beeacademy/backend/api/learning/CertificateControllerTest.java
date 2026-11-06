package com.beeacademy.backend.api.learning;

import com.beeacademy.backend.api.common.GlobalExceptionHandler;
import com.beeacademy.backend.application.learning.dto.CertificateResult;
import com.beeacademy.backend.application.learning.usecase.GenerateCertificateUseCase;
import com.beeacademy.backend.platform.security.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("CertificateController MockMvc Tests")
class CertificateControllerTest {

    private MockMvc mockMvc;
    private GenerateCertificateUseCase generateCertificateUseCase;

    private final UUID studentId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        generateCertificateUseCase = Mockito.mock(GenerateCertificateUseCase.class);

        CertificateController controller = new CertificateController(generateCertificateUseCase);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        UserPrincipal principal = new UserPrincipal(studentId, "student@test.com", "STUDENT");
        var auth = new UsernamePasswordAuthenticationToken(principal, null, List.of(new SimpleGrantedAuthority("ROLE_STUDENT")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("GET /api/certificates/my -> Trả về danh sách chứng chỉ")
    void getMyCertificates_Success() throws Exception {
        UUID certId = UUID.randomUUID();
        CertificateResult result = new CertificateResult(
                certId, "CERT-2026-ABC123", studentId, UUID.randomUUID(),
                "Nguyễn Văn A", "Khóa học Spring Boot", Instant.now(), null, "http://localhost:3000/verify-certificate/CERT-2026-ABC123"
        );

        when(generateCertificateUseCase.getMyCertificates(studentId)).thenReturn(List.of(result));

        mockMvc.perform(get("/api/certificates/my")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].certificateCode").value("CERT-2026-ABC123"))
                .andExpect(jsonPath("$.data[0].studentName").value("Nguyễn Văn A"));
    }

    @Test
    @DisplayName("GET /api/certificates/{id}/download -> Trả về stream file PDF")
    void downloadCertificatePdf_Success() throws Exception {
        UUID certId = UUID.randomUUID();
        byte[] fakePdf = "%PDF-1.4 Fake PDF Content".getBytes(StandardCharsets.UTF_8);

        when(generateCertificateUseCase.downloadPdf(certId, studentId)).thenReturn(fakePdf);

        mockMvc.perform(get("/api/certificates/{id}/download", certId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"certificate-" + certId + ".pdf\""))
                .andExpect(content().bytes(fakePdf));
    }

    @Test
    @DisplayName("GET /api/certificates/public/verify/{code} -> Tra cứu chứng chỉ công khai không cần auth")
    void verifyCertificate_Success() throws Exception {
        String code = "CERT-2026-ABC123";
        CertificateResult result = new CertificateResult(
                UUID.randomUUID(), code, studentId, UUID.randomUUID(),
                "Nguyễn Văn A", "Khóa học Spring Boot", Instant.now(), null, "http://localhost:3000/verify-certificate/" + code
        );

        when(generateCertificateUseCase.verifyByCode(code)).thenReturn(result);

        mockMvc.perform(get("/api/certificates/public/verify/{code}", code)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.certificateCode").value(code))
                .andExpect(jsonPath("$.data.studentName").value("Nguyễn Văn A"));
    }
}
