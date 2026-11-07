package com.beeacademy.backend.api.commerce;

import com.beeacademy.backend.api.commerce.dto.TeacherBankRequest;
import com.beeacademy.backend.application.commerce.dto.PayoutPeriodResult;
import com.beeacademy.backend.application.commerce.dto.TeacherBankAccountResult;
import com.beeacademy.backend.application.commerce.dto.TeacherRevenueStatsResult;
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
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("TeacherFinanceController MockMvc Tests")
class TeacherFinanceControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private ManageTeacherBankUseCase manageTeacherBankUseCase;
    private ManagePayoutUseCase managePayoutUseCase;

    private final UUID teacherId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        manageTeacherBankUseCase = Mockito.mock(ManageTeacherBankUseCase.class);
        managePayoutUseCase = Mockito.mock(ManagePayoutUseCase.class);

        TeacherFinanceController controller = new TeacherFinanceController(
                manageTeacherBankUseCase, managePayoutUseCase
        );

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        UserPrincipal principal = new UserPrincipal(teacherId, "teacher@bee.edu.vn", "ROLE_TEACHER");
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority("ROLE_TEACHER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("GET /api/teacher/finance/bank-account - Xem thông tin tài khoản ngân hàng")
    void getMyBankAccount_Success() throws Exception {
        TeacherBankAccountResult result = new TeacherBankAccountResult(
                UUID.randomUUID(), teacherId, "Vietcombank", "******6789", "TRAN VAN C", "CN Ba Đình",
                "VERIFIED", true, Instant.now(), Instant.now()
        );

        when(manageTeacherBankUseCase.getBankAccount(eq(teacherId))).thenReturn(Optional.of(result));

        mockMvc.perform(get("/api/teacher/finance/bank-account"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.bankName").value("Vietcombank"))
                .andExpect(jsonPath("$.data.accountNumberMasked").value("******6789"))
                .andExpect(jsonPath("$.data.verifyStatus").value("VERIFIED"));
    }

    @Test
    @DisplayName("POST /api/teacher/finance/bank-account - Cập nhật tài khoản ngân hàng")
    void updateBankAccount_Success() throws Exception {
        TeacherBankRequest request = new TeacherBankRequest("Vietcombank", "0123456789", "TRAN VAN C", "CN Ba Đình");

        TeacherBankAccountResult result = new TeacherBankAccountResult(
                UUID.randomUUID(), teacherId, "Vietcombank", "******6789", "TRAN VAN C", "CN Ba Đình",
                "PENDING", false, Instant.now(), Instant.now()
        );

        when(manageTeacherBankUseCase.saveOrUpdateBankAccount(eq(teacherId), any())).thenReturn(result);

        mockMvc.perform(post("/api/teacher/finance/bank-account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.verifyStatus").value("PENDING"));
    }

    @Test
    @DisplayName("GET /api/teacher/finance/statistics - Thống kê tài chính 80/20")
    void getFinanceStatistics_Success() throws Exception {
        TeacherRevenueStatsResult stats = new TeacherRevenueStatsResult(
                10000000, 2000000, 8000000, 3000000, 5000000, 25, "2026-05"
        );

        when(managePayoutUseCase.getTeacherRevenueStatistics(eq(teacherId))).thenReturn(stats);

        mockMvc.perform(get("/api/teacher/finance/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalGrossVnd").value(10000000))
                .andExpect(jsonPath("$.data.totalPlatformFeeVnd").value(2000000))
                .andExpect(jsonPath("$.data.totalTeacherVnd").value(8000000));
    }
}

