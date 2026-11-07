package com.beeacademy.backend.infrastructure.admin.persistence.adapter;

import com.beeacademy.backend.domain.admin.SystemSettings;
import com.beeacademy.backend.domain.admin.SystemSettingsRepositoryPort;
import com.beeacademy.backend.infrastructure.admin.persistence.entity.SystemSettingsJpaEntity;
import com.beeacademy.backend.infrastructure.admin.persistence.repository.SpringDataSystemSettingsRepository;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class SystemSettingsRepositoryAdapter implements SystemSettingsRepositoryPort {

    private final SpringDataSystemSettingsRepository repository;

    public SystemSettingsRepositoryAdapter(SpringDataSystemSettingsRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<SystemSettings> findByKey(String key) {
        if (key == null) return Optional.empty();
        return repository.findById(key)
                .map(e -> new SystemSettings(e.getKey(), e.getValueJson(), e.getUpdatedAt()));
    }

    @Override
    public SystemSettings save(SystemSettings settings) {
        SystemSettingsJpaEntity entity = repository.findById(settings.getKey())
                .orElse(new SystemSettingsJpaEntity(settings.getKey(), settings.getValue(), null, null, settings.getUpdatedAt()));

        entity.setValueJson(settings.getValue());
        entity.setUpdatedAt(settings.getUpdatedAt());

        SystemSettingsJpaEntity saved = repository.save(entity);
        return new SystemSettings(saved.getKey(), saved.getValueJson(), saved.getUpdatedAt());
    }

    @Override
    public Map<String, String> getAllSettingsAsMap() {
        return repository.findAll().stream()
                .collect(Collectors.toMap(
                        SystemSettingsJpaEntity::getKey,
                        SystemSettingsJpaEntity::getValueJson,
                        (existing, replacement) -> replacement
                ));
    }
}

