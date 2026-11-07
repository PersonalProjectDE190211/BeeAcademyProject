package com.beeacademy.backend.api.admin;

import com.beeacademy.backend.api.admin.dto.MaintenanceModeRequest;
import com.beeacademy.backend.api.admin.dto.UpdateSettingRequest;
import com.beeacademy.backend.api.common.GlobalExceptionHandler;
import com.beeacademy.backend.application.admin.dto.AdminAuditLogDto;
import com.beeacademy.backend.application.admin.usecase.ManageSystemSettingsUseCase;
import com.beeacademy.backend.platform.security.UserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("AdminSystemSettingsController MockMvc Tests")
class AdminSystemSettingsControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private ManageSystemSettingsUseCase manageSystemSettingsUseCase;
    private final UUID adminId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        manageSystemSettingsUseCase = Mockito.mock(ManageSystemSettingsUseCase.class);
        AdminSystemSettingsController controller = new AdminSystemSettingsController(manageSystemSettingsUseCase);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
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
    @DisplayName("GET /api/admin/settings trả về toàn bộ map cấu hình")
    void shouldGetAllSettings() throws Exception {
        when(manageSystemSettingsUseCase.getAllSettings()).thenReturn(Map.of(
                "maintenance_mode", "{\"enabled\":false}",
                "revenue_split_policy", "{\"platformFeeRate\":20.0}"
        ));

        mockMvc.perform(get("/api/admin/settings")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.maintenance_mode").value("{\"enabled\":false}"));
    }

    @Test
    @DisplayName("POST /api/admin/settings/maintenance bật chế độ bảo trì thành công")
    void shouldToggleMaintenanceMode() throws Exception {
        MaintenanceModeRequest req = new MaintenanceModeRequest(true, "Bảo trì nâng cấp hạ tầng mạng");

        mockMvc.perform(post("/api/admin/settings/maintenance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Đã kích hoạt chế độ bảo trì"));

        verify(manageSystemSettingsUseCase).toggleMaintenanceMode(true, "Bảo trì nâng cấp hạ tầng mạng", adminId);
    }

    @Test
    @DisplayName("PUT /api/admin/settings/{key} cập nhật cấu hình theo khóa")
    void shouldUpdateSetting() throws Exception {
        UpdateSettingRequest req = new UpdateSettingRequest("{\"platformFeeRate\":18.0}", "Điều chỉnh chính sách ưu đãi");

        mockMvc.perform(put("/api/admin/settings/revenue_split_policy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Cập nhật cấu hình thành công"));

        verify(manageSystemSettingsUseCase).updateSetting(eq("revenue_split_policy"), eq("{\"platformFeeRate\":18.0}"), eq("Điều chỉnh chính sách ưu đãi"), eq(adminId));
    }

    @Test
    @DisplayName("GET /api/admin/settings/audit-logs trả về danh sách kiểm toán phân trang")
    void shouldGetAuditLogs() throws Exception {
        AdminAuditLogDto log = new AdminAuditLogDto(
                UUID.randomUUID(),
                "USER_ACCOUNT",
                UUID.randomUUID(),
                "LOCK",
                adminId,
                "ADMIN",
                "Khóa do vi phạm",
                "{}",
                "{}",
                Instant.now()
        );
        when(manageSystemSettingsUseCase.getAuditLogs(any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(log), org.springframework.data.domain.PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/admin/settings/audit-logs")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].action").value("LOCK"));
    }
}

