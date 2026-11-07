package com.beeacademy.backend.application.admin;

import com.beeacademy.backend.application.admin.dto.AdminAuditLogDto;
import com.beeacademy.backend.application.admin.usecase.ManageSystemSettingsUseCase;
import com.beeacademy.backend.domain.admin.SystemSettings;
import com.beeacademy.backend.domain.admin.SystemSettingsRepositoryPort;
import com.beeacademy.backend.domain.common.AuditLog;
import com.beeacademy.backend.domain.common.AuditLogRepositoryPort;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.infrastructure.admin.persistence.entity.AuditLogJpaEntity;
import com.beeacademy.backend.infrastructure.admin.persistence.repository.SpringDataAuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManageSystemSettingsUseCaseTest {

    @Mock
    private SystemSettingsRepositoryPort settingsRepository;

    @Mock
    private AuditLogRepositoryPort auditLogRepository;

    @Mock
    private SpringDataAuditLogRepository springDataAuditLogRepository;

    @InjectMocks
    private ManageSystemSettingsUseCase useCase;

    private UUID adminId;

    @BeforeEach
    void setUp() {
        adminId = UUID.randomUUID();
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
    @DisplayName("isMaintenanceModeEnabled nhận biết trạng thái bật/tắt chính xác")
    void shouldCheckMaintenanceMode() {
        when(settingsRepository.findByKey(SystemSettings.KEY_MAINTENANCE_MODE))
                .thenReturn(Optional.of(SystemSettings.of(SystemSettings.KEY_MAINTENANCE_MODE, "{\"enabled\": true}")));

        assertThat(useCase.isMaintenanceModeEnabled()).isTrue();

        when(settingsRepository.findByKey(SystemSettings.KEY_MAINTENANCE_MODE))
                .thenReturn(Optional.of(SystemSettings.of(SystemSettings.KEY_MAINTENANCE_MODE, "{\"enabled\": false}")));

        assertThat(useCase.isMaintenanceModeEnabled()).isFalse();
    }

    @Test
    @DisplayName("toggleMaintenanceMode cập nhật cấu hình bảo trì và ghi audit log")
    void shouldToggleMaintenanceMode() {
        useCase.toggleMaintenanceMode(true, "Bảo trì nâng cấp cơ sở dữ liệu", adminId);

        verify(settingsRepository).save(any(SystemSettings.class));
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("updateSetting cập nhật cấu hình và ghi audit log")
    void shouldUpdateSetting() {
        useCase.updateSetting("revenue_split_policy", "{\"platformFeeRate\":15.0}", "Giảm phí nền tảng hè", adminId);

        verify(settingsRepository).save(any(SystemSettings.class));
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("updateSetting ném ngoại lệ khi key rỗng")
    void shouldThrowWhenKeyEmpty() {
        assertThatThrownBy(() -> useCase.updateSetting("", "{}", "Lý do", adminId))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("getAuditLogs trả về danh sách nhật ký kiểm toán phân trang")
    void shouldGetAuditLogs() {
        PageRequest pageable = PageRequest.of(0, 10);
        AuditLogJpaEntity logEntity = new AuditLogJpaEntity(
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
        when(springDataAuditLogRepository.findAllByOrderByCreatedAtDesc(pageable))
                .thenReturn(new PageImpl<>(List.of(logEntity)));

        Page<AdminAuditLogDto> page = useCase.getAuditLogs(null, pageable);

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).action()).isEqualTo("LOCK");
    }
}

