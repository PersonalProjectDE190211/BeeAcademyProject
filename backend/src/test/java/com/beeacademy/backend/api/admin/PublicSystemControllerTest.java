package com.beeacademy.backend.api.admin;

import com.beeacademy.backend.api.common.GlobalExceptionHandler;
import com.beeacademy.backend.application.admin.usecase.ManageSystemSettingsUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("PublicSystemController MockMvc Tests")
class PublicSystemControllerTest {

    private MockMvc mockMvc;
    private ManageSystemSettingsUseCase manageSystemSettingsUseCase;

    @BeforeEach
    void setUp() {
        manageSystemSettingsUseCase = Mockito.mock(ManageSystemSettingsUseCase.class);
        PublicSystemController controller = new PublicSystemController(manageSystemSettingsUseCase);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/public/system/maintenance trả về trạng thái bảo trì khi đang bảo trì")
    void shouldReturnMaintenanceTrue() throws Exception {
        when(manageSystemSettingsUseCase.isMaintenanceModeEnabled()).thenReturn(true);

        mockMvc.perform(get("/api/public/system/maintenance")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.enabled").value(true))
                .andExpect(jsonPath("$.data.message").value("Hệ thống BeeAcademy đang bảo trì nâng cấp định kỳ. Vui lòng quay lại sau!"));
    }

    @Test
    @DisplayName("GET /api/public/system/maintenance trả về trạng thái bình thường khi không bảo trì")
    void shouldReturnMaintenanceFalse() throws Exception {
        when(manageSystemSettingsUseCase.isMaintenanceModeEnabled()).thenReturn(false);

        mockMvc.perform(get("/api/public/system/maintenance")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.enabled").value(false))
                .andExpect(jsonPath("$.data.message").value("Hệ thống hoạt động bình thường"));
    }
}

