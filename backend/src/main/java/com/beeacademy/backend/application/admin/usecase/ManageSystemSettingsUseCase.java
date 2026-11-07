package com.beeacademy.backend.application.admin.usecase;

import com.beeacademy.backend.application.admin.dto.AdminAuditLogDto;
import com.beeacademy.backend.domain.admin.SystemSettings;
import com.beeacademy.backend.domain.admin.SystemSettingsRepositoryPort;
import com.beeacademy.backend.domain.common.AuditEntityType;
import com.beeacademy.backend.domain.common.AuditLog;
import com.beeacademy.backend.domain.common.AuditLogRepositoryPort;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.common.PageResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ManageSystemSettingsUseCase {

    private static final String DEFAULT_MAINTENANCE_MSG =
            "Hệ thống BeeAcademy đang bảo trì nâng cấp định kỳ. Vui lòng quay lại sau!";
    private static final String NORMAL_OPERATION_MSG = "Hệ thống hoạt động bình thường";

    private final SystemSettingsRepositoryPort settingsRepository;
    private final AuditLogRepositoryPort auditLogRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public ManageSystemSettingsUseCase(
            SystemSettingsRepositoryPort settingsRepository,
            AuditLogRepositoryPort auditLogRepository,
            ObjectMapper objectMapper
    ) {
        this.settingsRepository = settingsRepository;
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }

    public ManageSystemSettingsUseCase(
            SystemSettingsRepositoryPort settingsRepository,
            AuditLogRepositoryPort auditLogRepository
    ) {
        this(settingsRepository, auditLogRepository, new ObjectMapper());
    }

    @Transactional(readOnly = true)
    public Map<String, String> getAllSettings() {
        return settingsRepository.getAllSettingsAsMap();
    }

    @Transactional(readOnly = true)
    public MaintenanceStatus getMaintenanceStatus() {
        return settingsRepository.findByKey(SystemSettings.KEY_MAINTENANCE_MODE)
                .map(setting -> parseMaintenanceSetting(setting.getValue()))
                .orElseGet(() -> new MaintenanceStatus(false, NORMAL_OPERATION_MSG));
    }

    @Transactional(readOnly = true)
    public boolean isMaintenanceModeEnabled() {
        return getMaintenanceStatus().enabled();
    }

    @Transactional
    public void toggleMaintenanceMode(boolean enabled, String message, UUID adminId) {
        String msg = (message != null && !message.isBlank()) ? message.trim() : DEFAULT_MAINTENANCE_MSG;
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("enabled", enabled);
        payload.put("message", msg);

        String valueJson = toJson(payload);
        SystemSettings setting = SystemSettings.of(SystemSettings.KEY_MAINTENANCE_MODE, valueJson);
        settingsRepository.save(setting);

        AuditLog audit = AuditLog.record(
                AuditEntityType.SYSTEM_SETTING,
                UUID.nameUUIDFromBytes(SystemSettings.KEY_MAINTENANCE_MODE.getBytes(StandardCharsets.UTF_8)),
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
        validateJsonObject(valueJson);

        String normalizedKey = key.trim();
        String normalizedValue = valueJson.trim();
        SystemSettings setting = SystemSettings.of(normalizedKey, normalizedValue);
        settingsRepository.save(setting);

        AuditLog audit = AuditLog.record(
                AuditEntityType.SYSTEM_SETTING,
                UUID.nameUUIDFromBytes(normalizedKey.getBytes(StandardCharsets.UTF_8)),
                "UPDATE_SYSTEM_SETTING",
                adminId,
                "ADMIN",
                reason != null && !reason.isBlank() ? reason.trim() : "Cập nhật cấu hình hệ thống: " + normalizedKey,
                normalizedValue
        );
        auditLogRepository.save(audit);
    }

    @Transactional(readOnly = true)
    public Page<AdminAuditLogDto> getAuditLogs(String entityType, Pageable pageable) {
        int page = pageable != null && pageable.isPaged() ? pageable.getPageNumber() : 0;
        int size = pageable != null && pageable.isPaged() ? pageable.getPageSize() : 20;

        AuditEntityType filterType = null;
        if (entityType != null && !entityType.isBlank()) {
            try {
                filterType = AuditEntityType.valueOf(entityType.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new DomainException("INVALID_AUDIT_ENTITY_TYPE",
                        "Loại thực thể kiểm toán không hợp lệ: " + entityType);
            }
        }

        PageResult<AuditLog> result = auditLogRepository.findPaged(filterType, page, size);
        List<AdminAuditLogDto> content = result.content().stream()
                .map(log -> new AdminAuditLogDto(
                        log.getId(),
                        log.getEntityType().name(),
                        log.getEntityId(),
                        log.getAction(),
                        log.getActorId(),
                        log.getActorRole(),
                        log.getReason(),
                        log.getDiffJson(),
                        log.getMetadataJson(),
                        log.getCreatedAt()
                ))
                .toList();

        return new PageImpl<>(content, PageRequest.of(result.page(), Math.max(1, result.size())), result.totalElements());
    }

    private MaintenanceStatus parseMaintenanceSetting(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return new MaintenanceStatus(false, NORMAL_OPERATION_MSG);
        }
        try {
            JsonNode node = objectMapper.readTree(rawValue);
            boolean enabled = node.path("enabled").asBoolean(false);
            String configuredMsg = node.path("message").asText("").trim();
            String message = enabled
                    ? (configuredMsg.isEmpty() ? DEFAULT_MAINTENANCE_MSG : configuredMsg)
                    : NORMAL_OPERATION_MSG;
            return new MaintenanceStatus(enabled, message);
        } catch (JsonProcessingException e) {
            boolean fallbackEnabled = "true".equalsIgnoreCase(rawValue.trim()) || "1".equals(rawValue.trim());
            return new MaintenanceStatus(fallbackEnabled, fallbackEnabled ? DEFAULT_MAINTENANCE_MSG : NORMAL_OPERATION_MSG);
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new DomainException("JSON_SERIALIZATION_ERROR", "Không thể mã hóa JSON cấu hình hệ thống");
        }
    }

    private void validateJsonObject(String json) {
        try {
            objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new DomainException("INVALID_SETTING_JSON", "Giá trị cấu hình phải là chuỗi JSON hợp lệ");
        }
    }

    public record MaintenanceStatus(boolean enabled, String message) {
    }
}
