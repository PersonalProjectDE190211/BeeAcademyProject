package com.beeacademy.backend.infrastructure.admin.persistence.repository;

import com.beeacademy.backend.infrastructure.admin.persistence.entity.SystemSettingsJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataSystemSettingsRepository extends JpaRepository<SystemSettingsJpaEntity, String> {
}

