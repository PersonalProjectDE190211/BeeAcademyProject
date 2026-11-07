package com.beeacademy.backend.application.admin;

import com.beeacademy.backend.application.admin.dto.AdminAuditLogDto;
import com.beeacademy.backend.application.admin.usecase.ManageSystemSettingsUseCase;
import com.beeacademy.backend.domain.admin.SystemSettings;
import com.beeacademy.backend.domain.admin.SystemSettingsRepositoryPort;
import com.beeacademy.backend.domain.common.AuditEntityType;
import com.beeacademy.backend.domain.common.AuditLog;
import com.beeacademy.backend.domain.common.AuditLogRepositoryPort;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.common.PageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManageSystemSettingsUseCaseTest {

    @Mock
    private SystemSettingsRepositoryPort settingsRepository;

    @Mock
    private AuditLogRepositoryPort auditLogRepository;

    private ManageSystemSettingsUseCase useCase;

    private UUID adminId;

    @BeforeEach
    void setUp() {
        adminId = UUID.randomUUID();
        useCase = new ManageSystemSettingsUseCase(settingsRepository, auditLogRepository);
    }

    @Test
    @DisplayName("getAllSettings trả về toàn bộ map cấu hình hệ thống")
    void shouldReturnAllSettings() {
        when(settingsRepository.getAllSettingsAsMap()).thenReturn(Map.of(
                "maintenance_mode", "{\"enabled\":false}",
                "revenue_split_policy", "{\"platformFeeRate\":20.0}"
        ));

        Map<String, String> settings = useCase.getAllSettings();

        assertThat(settings).hasSize(2);
        assertThat(settings).containsKey("maintenance_mode");
    }

    @Test
    @DisplayName("getMaintenanceStatus và isMaintenanceModeEnabled parse JSON bằng Jackson và lấy đúng message từ DB")
    void shouldParseMaintenanceStatusWithJackson() {
        when(settingsRepository.findByKey(SystemSettings.KEY_MAINTENANCE_MODE))
                .thenReturn(Optional.of(SystemSettings.of(
                        SystemSettings.KEY_MAINTENANCE_MODE,
                        "{\"message\":\"Bảo trì lúc 23:00\",\"enabled\":true}"
                )));

        ManageSystemSettingsUseCase.MaintenanceStatus status = useCase.getMaintenanceStatus();
        assertThat(status.enabled()).isTrue();
        assertThat(status.message()).isEqualTo("Bảo trì lúc 23:00");
        assertThat(useCase.isMaintenanceModeEnabled()).isTrue();

        when(settingsRepository.findByKey(SystemSettings.KEY_MAINTENANCE_MODE))
                .thenReturn(Optional.of(SystemSettings.of(
                        SystemSettings.KEY_MAINTENANCE_MODE,
                        "{\"enabled\":false,\"message\":\"Bảo trì lúc 23:00\"}"
                )));

        assertThat(useCase.isMaintenanceModeEnabled()).isFalse();
        assertThat(useCase.getMaintenanceStatus().message()).isEqualTo("Hệ thống hoạt động bình thường");
    }

    @Test
    @DisplayName("toggleMaintenanceMode thoát chuỗi ký tự đặc biệt an toàn bằng Jackson và ghi audit log")
    void shouldToggleMaintenanceModeSafely() {
        useCase.toggleMaintenanceMode(true, "Bảo trì \"nâng cấp\" cơ sở dữ liệu", adminId);

        ArgumentCaptor<SystemSettings> captor = ArgumentCaptor.forClass(SystemSettings.class);
        verify(settingsRepository).save(captor.capture());
        assertThat(captor.getValue().getValue())
                .isEqualTo("{\"enabled\":true,\"message\":\"Bảo trì \\\"nâng cấp\\\" cơ sở dữ liệu\"}");
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("updateSetting cập nhật cấu hình JSON hợp lệ và ghi audit log")
    void shouldUpdateSetting() {
        useCase.updateSetting("revenue_split_policy", "{\"platformFeeRate\":15.0}", "Giảm phí nền tảng hè", adminId);

        verify(settingsRepository).save(any(SystemSettings.class));
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("updateSetting ném ngoại lệ khi key rỗng hoặc JSON không hợp lệ")
    void shouldThrowWhenKeyEmptyOrInvalidJson() {
        assertThatThrownBy(() -> useCase.updateSetting("", "{}", "Lý do", adminId))
                .isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> useCase.updateSetting("key", "{broken json", "Lý do", adminId))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("getAuditLogs trả về danh sách nhật ký kiểm toán phân trang qua AuditLogRepositoryPort")
    void shouldGetAuditLogs() {
        PageRequest pageable = PageRequest.of(0, 10);
        AuditLog logDomain = new AuditLog(
                UUID.randomUUID(),
                AuditEntityType.USER_ACCOUNT,
                UUID.randomUUID(),
                "LOCK",
                adminId,
                "ADMIN",
                "Khóa do vi phạm",
                "{}",
                "{}",
                Instant.now()
        );
        when(auditLogRepository.findPaged(null, 0, 10))
                .thenReturn(new PageResult<>(List.of(logDomain), 0, 10, 1));

        Page<AdminAuditLogDto> page = useCase.getAuditLogs(null, pageable);

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).action()).isEqualTo("LOCK");
    }
}
