package com.beeacademy.backend.infrastructure.admin;

import com.beeacademy.backend.domain.common.AuditEntityType;
import com.beeacademy.backend.domain.common.AuditLog;
import com.beeacademy.backend.infrastructure.admin.persistence.adapter.AuditLogRepositoryAdapter;
import com.beeacademy.backend.infrastructure.admin.persistence.entity.AuditLogJpaEntity;
import com.beeacademy.backend.infrastructure.admin.persistence.repository.SpringDataAuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogRepositoryAdapterTest {

    @Mock
    private SpringDataAuditLogRepository repository;

    @InjectMocks
    private AuditLogRepositoryAdapter adapter;

    private UUID entityId;
    private UUID actorId;
    private AuditLogJpaEntity entity;

    @BeforeEach
    void setUp() {
        entityId = UUID.randomUUID();
        actorId = UUID.randomUUID();
        entity = new AuditLogJpaEntity(
                UUID.randomUUID(),
                "USER_ACCOUNT",
                entityId,
                "LOCK",
                actorId,
                "ADMIN",
                "Vi phạm nội quy",
                "{\"status\":\"locked\"}",
                "{}",
                Instant.now()
        );
    }

    @Test
    @DisplayName("save lưu AuditLog và trả về domain object")
    void shouldSaveAuditLog() {
        when(repository.save(any(AuditLogJpaEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuditLog log = AuditLog.record(
                AuditEntityType.USER_ACCOUNT,
                entityId,
                "LOCK",
                actorId,
                "ADMIN",
                "Vi phạm nội quy",
                "{\"status\":\"locked\"}"
        );

        AuditLog saved = adapter.save(log);

        assertThat(saved.getEntityType()).isEqualTo(AuditEntityType.USER_ACCOUNT);
        assertThat(saved.getAction()).isEqualTo("LOCK");
        assertThat(saved.getEntityId()).isEqualTo(entityId);
        verify(repository).save(any(AuditLogJpaEntity.class));
    }

    @Test
    @DisplayName("findByEntity trả về danh sách audit log theo entityType và entityId")
    void shouldFindByEntity() {
        when(repository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc("USER_ACCOUNT", entityId))
                .thenReturn(List.of(entity));

        List<AuditLog> results = adapter.findByEntity(AuditEntityType.USER_ACCOUNT, entityId);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getEntityId()).isEqualTo(entityId);
        assertThat(results.get(0).getAction()).isEqualTo("LOCK");
    }

    @Test
    @DisplayName("findByActorId trả về danh sách audit log do actor tạo")
    void shouldFindByActorId() {
        when(repository.findByActorIdOrderByCreatedAtDesc(actorId)).thenReturn(List.of(entity));

        List<AuditLog> results = adapter.findByActorId(actorId);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getActorId()).isEqualTo(actorId);
    }
}

