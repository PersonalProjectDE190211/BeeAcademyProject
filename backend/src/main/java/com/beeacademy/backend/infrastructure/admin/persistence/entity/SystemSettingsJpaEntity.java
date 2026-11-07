package com.beeacademy.backend.infrastructure.admin.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "system_settings")
public class SystemSettingsJpaEntity {

    @Id
    @Column(name = "key", length = 100, nullable = false)
    private String key;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "value", columnDefinition = "jsonb", nullable = false)
    private String valueJson = "{}";

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public SystemSettingsJpaEntity() {
    }

    public SystemSettingsJpaEntity(String key, String valueJson, String description, UUID updatedBy, Instant updatedAt) {
        this.key = key;
        this.valueJson = valueJson != null ? valueJson : "{}";
        this.description = description;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValueJson() {
        return valueJson;
    }

    public void setValueJson(String valueJson) {
        this.valueJson = valueJson;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UUID getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(UUID updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}

