package com.beeacademy.backend.infrastructure.admin;

import com.beeacademy.backend.domain.admin.SystemSettings;
import com.beeacademy.backend.infrastructure.admin.persistence.adapter.SystemSettingsRepositoryAdapter;
import com.beeacademy.backend.infrastructure.admin.persistence.entity.SystemSettingsJpaEntity;
import com.beeacademy.backend.infrastructure.admin.persistence.repository.SpringDataSystemSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SystemSettingsRepositoryAdapterTest {

    @Mock
    private SpringDataSystemSettingsRepository repository;

    @InjectMocks
    private SystemSettingsRepositoryAdapter adapter;

    private SystemSettingsJpaEntity entity;

    @BeforeEach
    void setUp() {
        entity = new SystemSettingsJpaEntity("maintenance_mode", "{\"enabled\":false}", "Chế độ bảo trì", null, Instant.now());
    }

    @Test
    @DisplayName("findByKey trả về SystemSettings khi tồn tại")
    void shouldFindKeySuccessfully() {
        when(repository.findById("maintenance_mode")).thenReturn(Optional.of(entity));

        Optional<SystemSettings> result = adapter.findByKey("maintenance_mode");

        assertThat(result).isPresent();
        assertThat(result.get().getKey()).isEqualTo("maintenance_mode");
        assertThat(result.get().getValue()).isEqualTo("{\"enabled\":false}");
    }

    @Test
    @DisplayName("save lưu và chuyển đổi SystemSettings chính xác")
    void shouldSaveSuccessfully() {
        when(repository.findById("maintenance_mode")).thenReturn(Optional.of(entity));
        when(repository.save(any(SystemSettingsJpaEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SystemSettings toSave = SystemSettings.of("maintenance_mode", "{\"enabled\":true}");
        SystemSettings saved = adapter.save(toSave);

        assertThat(saved.getKey()).isEqualTo("maintenance_mode");
        assertThat(saved.getValue()).isEqualTo("{\"enabled\":true}");
        verify(repository).save(any(SystemSettingsJpaEntity.class));
    }

    @Test
    @DisplayName("getAllSettingsAsMap trả về Map ánh xạ các cấu hình")
    void shouldGetAllSettingsAsMap() {
        SystemSettingsJpaEntity e2 = new SystemSettingsJpaEntity("revenue_split_policy", "{\"platformFeeRate\":20.0}", null, null, Instant.now());
        when(repository.findAll()).thenReturn(List.of(entity, e2));

        Map<String, String> map = adapter.getAllSettingsAsMap();

        assertThat(map).hasSize(2);
        assertThat(map).containsEntry("maintenance_mode", "{\"enabled\":false}");
        assertThat(map).containsEntry("revenue_split_policy", "{\"platformFeeRate\":20.0}");
    }
}

