package com.beeacademy.backend.api.admin;

import com.beeacademy.backend.api.common.GlobalExceptionHandler;
import com.beeacademy.backend.application.admin.dto.AdminDashboardDto;
import com.beeacademy.backend.application.admin.usecase.GetAdminDashboardUseCase;
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

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("AdminDashboardController MockMvc Tests")
class AdminDashboardControllerTest {

    private MockMvc mockMvc;
    private GetAdminDashboardUseCase getAdminDashboardUseCase;
    private final UUID adminId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        getAdminDashboardUseCase = Mockito.mock(GetAdminDashboardUseCase.class);
        AdminDashboardController controller = new AdminDashboardController(getAdminDashboardUseCase);

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
    @DisplayName("GET /api/admin/dashboard/summary trả về 200 và số liệu thống kê")
    void shouldReturnDashboardSummary() throws Exception {
        AdminDashboardDto dto = new AdminDashboardDto(
                20_000_000L,
                4_000_000L,
                16_000_000L,
                500L,
                400L,
                60L,
                38L,
                2L,
                480L,
                20L,
                5L,
                50L,
                40L,
                5L,
                3L,
                2L,
                1200L,
                8L
        );
        when(getAdminDashboardUseCase.execute()).thenReturn(dto);

        mockMvc.perform(get("/api/admin/dashboard/summary")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalGmv").value(20000000))
                .andExpect(jsonPath("$.data.platformFee").value(4000000))
                .andExpect(jsonPath("$.data.teacherAmount").value(16000000))
                .andExpect(jsonPath("$.data.totalUsers").value(500))
                .andExpect(jsonPath("$.data.studentCount").value(400))
                .andExpect(jsonPath("$.data.teacherCount").value(60))
                .andExpect(jsonPath("$.data.totalCourses").value(50));
    }
}

