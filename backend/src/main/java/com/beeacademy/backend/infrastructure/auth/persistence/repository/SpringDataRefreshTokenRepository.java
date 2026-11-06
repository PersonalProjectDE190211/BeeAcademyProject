package com.beeacademy.backend.infrastructure.auth.persistence.repository;

import com.beeacademy.backend.infrastructure.auth.persistence.entity.RefreshTokenJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository cho bảng 'refresh_tokens'.
 */
@Repository
public interface SpringDataRefreshTokenRepository extends JpaRepository<RefreshTokenJpaEntity, UUID> {

    Optional<RefreshTokenJpaEntity> findByTokenHash(String tokenHash);

    @Modifying
    @Query("UPDATE RefreshTokenJpaEntity r SET r.revoked = true, r.revokedAt = :now, r.revokeReason = 'MASS_REVOCATION' WHERE r.userId = :userId AND r.revoked = false")
    void revokeAllByUserId(@Param("userId") UUID userId, @Param("now") Instant now);

    @Modifying
    @Query("UPDATE RefreshTokenJpaEntity r SET r.revoked = true, r.revokedAt = :now, r.revokeReason = 'FAMILY_THEFT_DETECTED' WHERE r.familyId = :familyId AND r.revoked = false")
    void revokeAllByFamilyId(@Param("familyId") UUID familyId, @Param("now") Instant now);

    @Modifying
    @Query("DELETE FROM RefreshTokenJpaEntity r WHERE r.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") Instant now);
}

