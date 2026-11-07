package com.beeacademy.backend.application.admin.usecase;

import com.beeacademy.backend.application.admin.dto.AdminAuditLogDto;
import com.beeacademy.backend.domain.admin.SystemSettings;
import com.beeacademy.backend.domain.admin.SystemSettingsRepositoryPort;
import com.beeacademy.backend.domain.common.AuditEntityType;
import com.beeacademy.backend.domain.common.AuditLog;
import com.beeacademy.backend.domain.common.AuditLogRepositoryPort;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.infrastructure.admin.persistence.repository.SpringDataAuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class ManageSystemSettingsUseCase {

    private final SystemSettingsRepositoryPort settingsRepository;
    private final AuditLogRepositoryPort auditLogRepository;
    private final SpringDataAuditLogRepository springDataAuditLogRepository;

    public ManageSystemSettingsUseCase(
            SystemSettingsRepositoryPort settingsRepository,
            AuditLogRepositoryPort auditLogRepository,
            SpringDataAuditLogRepository springDataAuditLogRepository
    ) {
        this.settingsRepository = settingsRepository;
        this.auditLogRepository = auditLogRepository;
        this.springDataAuditLogRepository = springDataAuditLogRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, String> getAllSettings() {
        return settingsRepository.getAllSettingsAsMap();
    }

    @Transactional(readOnly = true)
    public boolean isMaintenanceModeEnabled() {
        return settingsRepository.findByKey(SystemSettings.KEY_MAINTENANCE_MODE)
                .map(s -> s.getValue() != null && s.getValue().contains("\"enabled\": true") || (s.getValue() != null && s.getValue().contains("\"enabled\":true")))
                .orElse(false);
    }

    @Transactional
    public void toggleMaintenanceMode(boolean enabled, String message, UUID adminId) {
        String msg = (message != null && !message.isBlank()) ? message.trim() : "Hệ thống đang bảo trì";
        String valueJson = String.format("{\"enabled\": %b, \"message\": \"%s\"}", enabled, msg);

        SystemSettings setting = SystemSettings.of(SystemSettings.KEY_MAINTENANCE_MODE, valueJson);
        settingsRepository.save(setting);

        AuditLog audit = AuditLog.record(
                AuditEntityType.SYSTEM_SETTING,
                UUID.nameUUIDFromBytes(SystemSettings.KEY_MAINTENANCE_MODE.getBytes()),
                "UPDATE_MAINTENANCE_MODE",
                adminId,
                "ADMIN",
                msg,
                valueJson
        );
        auditLogRepository.save(audit);
    }

    @Transactional
    public void updateSetting(String key, String valueJson, String reason, UUID adminId) {
        if (key == null || key.isBlank()) {
            throw new DomainException("INVALID_SETTING_KEY", "Khóa cấu hình không được để trống");
        }
        if (valueJson == null || valueJson.isBlank()) {
            throw new DomainException("INVALID_SETTING_VALUE", "Giá trị cấu hình không được để trống");
        }

        SystemSettings setting = SystemSettings.of(key.trim(), valueJson.trim());
        settingsRepository.save(setting);

        AuditLog audit = AuditLog.record(
                AuditEntityType.SYSTEM_SETTING,
                UUID.nameUUIDFromBytes(key.getBytes()),
                "UPDATE_SYSTEM_SETTING",
                adminId,
                "ADMIN",
                reason != null ? reason.trim() : "Cập nhật cấu hình hệ thống: " + key,
                valueJson
        );
        auditLogRepository.save(audit);
    }

    @Transactional(readOnly = true)
    public Page<AdminAuditLogDto> getAuditLogs(String entityType, Pageable pageable) {
        if (entityType != null && !entityType.isBlank()) {
            return springDataAuditLogRepository.findByEntityTypeOrderByCreatedAtDesc(entityType.trim().toUpperCase(), pageable)
                    .map(e -> new AdminAuditLogDto(
                            e.getId(),
                            e.getEntityType(),
                            e.getEntityId(),
                            e.getAction(),
                            e.getActorId(),
                            e.getActorRole(),
                            e.getReason(),
                            e.getDiffJson(),
                            e.getMetadataJson(),
                            e.getCreatedAt()
                    ));
        }

        return springDataAuditLogRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(e -> new AdminAuditLogDto(
                        e.getId(),
                        e.getEntityType(),
                        e.getEntityId(),
                        e.getAction(),
                        e.getActorId(),
                        e.getActorRole(),
                        e.getReason(),
                        e.getDiffJson(),
                        e.getMetadataJson(),
                        e.getCreatedAt()
                ));
    }
}

