package com.beeacademy.backend.domain.admin;

import com.beeacademy.backend.domain.common.AbstractAggregateRoot;
import com.beeacademy.backend.domain.common.DomainException;

import java.time.Instant;

/**
 * Aggregate Root quản lý cấu hình hệ thống và chế độ bảo trì (Maintenance Mode).
 * Cho phép Admin kích hoạt/hủy kích hoạt bảo trì toàn hệ thống và lưu vết thời điểm bắt đầu.
 */
public class SystemSettings extends AbstractAggregateRoot {

    public static final String KEY_MAINTENANCE_MODE = "maintenance_mode";
    public static final String KEY_MAINTENANCE_MESSAGE = "maintenance_message";
    public static final String KEY_MAINTENANCE_STARTED_AT = "maintenance_started_at";

    private final String key;
    private String value;
    private Instant updatedAt;

    public SystemSettings(String key, String value, Instant updatedAt) {
        if (key == null || key.isBlank()) {
            throw new DomainException("INVALID_SETTING_KEY", "Khóa cấu hình không được để trống");
        }
        this.key = key.trim();
        this.value = value;
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public static SystemSettings of(String key, String value) {
        return new SystemSettings(key, value, Instant.now());
    }

    public void updateValue(String newValue) {
        this.value = newValue;
        this.updatedAt = Instant.now();
    }

    public boolean getAsBoolean(boolean defaultValue) {
        if (value == null || value.isBlank()) return defaultValue;
        return "true".equalsIgnoreCase(value.trim()) || "1".equals(value.trim());
    }

    public String getKey() { return key; }
    public String getValue() { return value; }
    public Instant getUpdatedAt() { return updatedAt; }
}

