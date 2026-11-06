package com.beeacademy.backend.infrastructure.auth.persistence.adapter;

import com.beeacademy.backend.domain.auth.RefreshToken;
import com.beeacademy.backend.domain.auth.RefreshTokenRepositoryPort;
import com.beeacademy.backend.infrastructure.auth.persistence.entity.RefreshTokenJpaEntity;
import com.beeacademy.backend.infrastructure.auth.persistence.repository.SpringDataRefreshTokenRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter triển khai Domain Port {@link RefreshTokenRepositoryPort} bằng Spring Data JPA.
 */
@Component
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepositoryPort {

    private final SpringDataRefreshTokenRepository repository;

    public RefreshTokenRepositoryAdapter(SpringDataRefreshTokenRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return repository.findByTokenHash(tokenHash).map(this::toDomain);
    }

    @Override
    @Transactional
    public RefreshToken save(RefreshToken token) {
        RefreshTokenJpaEntity entity = toEntity(token);
        RefreshTokenJpaEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional
    public void revokeAllByUserId(UUID userId, Instant now) {
        repository.revokeAllByUserId(userId, now);
    }

    @Override
    @Transactional
    public void deleteExpiredTokens(Instant now) {
        repository.deleteExpiredTokens(now);
    }

    private RefreshToken toDomain(RefreshTokenJpaEntity entity) {
        return RefreshToken.rehydrate(
                entity.getId(),
                entity.getUserId(),
                entity.getTokenHash(),
                entity.getFamilyId(),
                entity.getExpiresAt(),
                entity.getCreatedAt(),
                entity.getRevokedAt(),
                entity.getReplacedByTokenHash()
        );
    }

    private RefreshTokenJpaEntity toEntity(RefreshToken domain) {
        boolean isRevoked = domain.isRevoked();
        return new RefreshTokenJpaEntity(
                domain.getId(),
                domain.getUserId(),
                domain.getTokenHash(),
                domain.getFamilyId(),
                domain.getReplacedByTokenHash(),
                null, // deviceInfo
                null, // ipAddress
                domain.getExpiresAt(),
                isRevoked,
                domain.getRevokedAt(),
                isRevoked ? "REVOKED_ROTATION_OR_LOGOUT" : null,
                domain.getCreatedAt()
        );
    }
}

