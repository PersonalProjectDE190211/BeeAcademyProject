package com.beeacademy.backend.api.commerce;

import com.beeacademy.backend.api.commerce.dto.ConfirmPayoutRequest;
import com.beeacademy.backend.api.commerce.dto.RejectBankRequest;
import com.beeacademy.backend.application.commerce.dto.PayoutPeriodResult;
import com.beeacademy.backend.application.commerce.dto.TeacherBankAccountResult;
import com.beeacademy.backend.application.commerce.usecase.ManagePayoutUseCase;
import com.beeacademy.backend.application.commerce.usecase.ManageTeacherBankUseCase;
import com.beeacademy.backend.api.common.GlobalExceptionHandler;
import com.beeacademy.backend.platform.security.UserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("AdminPayoutController MockMvc Tests")
class AdminPayoutControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private ManagePayoutUseCase managePayoutUseCase;
    private ManageTeacherBankUseCase manageTeacherBankUseCase;

    private final UUID adminId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        managePayoutUseCase = Mockito.mock(ManagePayoutUseCase.class);
        manageTeacherBankUseCase = Mockito.mock(ManageTeacherBankUseCase.class);

        AdminPayoutController controller = new AdminPayoutController(managePayoutUseCase, manageTeacherBankUseCase);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        UserPrincipal principal = new UserPrincipal(adminId, "admin@bee.edu.vn", "ROLE_ADMIN");
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("GET /api/admin/finance/payouts - Xem danh sách các kỳ quyết toán")
    void getPayouts_Success() throws Exception {
        PayoutPeriodResult period = new PayoutPeriodResult(
                UUID.randomUUID(), UUID.randomUUID(), "2026-05", "PENDING", 5000000, 1000000, 4000000,
                10, null, null, null, null, null, Instant.now(), Instant.now()
        );

        when(managePayoutUseCase.getAllPayouts(any())).thenReturn(List.of(period));

        mockMvc.perform(get("/api/admin/finance/payouts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].monthYear").value("2026-05"))
                .andExpect(jsonPath("$.data[0].status").value("PENDING"));
    }

    @Test
    @DisplayName("POST /api/admin/finance/payouts/{id}/confirm - Xác nhận chi trả thành công")
    void confirmPayout_Success() throws Exception {
        UUID payoutPeriodId = UUID.randomUUID();
        ConfirmPayoutRequest request = new ConfirmPayoutRequest(
                "FT_202605_999", "Chuyển khoản lương tháng 5", "https://example.com/unc.pdf"
        );

        PayoutPeriodResult result = new PayoutPeriodResult(
                payoutPeriodId, UUID.randomUUID(), "2026-05", "PAID", 5000000, 1000000, 4000000,
                10, Instant.now(), adminId, "FT_202605_999", "Chuyển khoản lương tháng 5", "https://example.com/unc.pdf",
                Instant.now(), Instant.now()
        );

        when(managePayoutUseCase.confirmPayout(any())).thenReturn(result);

        mockMvc.perform(post("/api/admin/finance/payouts/" + payoutPeriodId + "/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PAID"))
                .andExpect(jsonPath("$.data.transferRef").value("FT_202605_999"));
    }

    @Test
    @DisplayName("POST /api/admin/finance/bank-accounts/{id}/verify - Duyệt tài khoản ngân hàng")
    void verifyBankAccount_Success() throws Exception {
        UUID bankAccountId = UUID.randomUUID();
        TeacherBankAccountResult result = new TeacherBankAccountResult(
                bankAccountId, UUID.randomUUID(), "Techcombank", "******1122", "LE VAN D", "CN Đống Đa",
                "VERIFIED", false, Instant.now(), Instant.now()
        );

        when(manageTeacherBankUseCase.verifyBankAccount(eq(bankAccountId), eq(adminId))).thenReturn(result);

        mockMvc.perform(post("/api/admin/finance/bank-accounts/" + bankAccountId + "/verify"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.verifyStatus").value("VERIFIED"));
    }
}

