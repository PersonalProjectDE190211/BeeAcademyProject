package com.beeacademy.backend.infrastructure.auth.persistence.repository;

import com.beeacademy.backend.infrastructure.auth.persistence.entity.ExternalLoginJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataExternalLoginRepository extends JpaRepository<ExternalLoginJpaEntity, UUID> {

    Optional<ExternalLoginJpaEntity> findByProviderAndProviderUserId(String provider, String providerUserId);

    Optional<ExternalLoginJpaEntity> findByUserIdAndProvider(UUID userId, String provider);
}

