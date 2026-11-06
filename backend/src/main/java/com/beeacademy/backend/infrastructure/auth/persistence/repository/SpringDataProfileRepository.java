package com.beeacademy.backend.infrastructure.auth.persistence.repository;

import com.beeacademy.backend.infrastructure.auth.persistence.entity.ProfileJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository cho bảng 'profiles'.
 */
@Repository
public interface SpringDataProfileRepository extends JpaRepository<ProfileJpaEntity, UUID> {

    Optional<ProfileJpaEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}

