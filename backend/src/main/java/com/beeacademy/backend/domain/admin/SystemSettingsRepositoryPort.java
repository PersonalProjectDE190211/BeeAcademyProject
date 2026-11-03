package com.beeacademy.backend.domain.admin;

import java.util.Map;
import java.util.Optional;

/**
 * Domain Repository Port cho Aggregate Root {@link SystemSettings}.
 */
public interface SystemSettingsRepositoryPort {

    Optional<SystemSettings> findByKey(String key);

    SystemSettings save(SystemSettings settings);

    Map<String, String> getAllSettingsAsMap();
}

