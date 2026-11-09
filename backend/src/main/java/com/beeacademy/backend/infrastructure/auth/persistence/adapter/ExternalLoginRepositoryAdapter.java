package com.beeacademy.backend.infrastructure.auth.persistence.adapter;

import com.beeacademy.backend.domain.auth.ExternalLogin;
import com.beeacademy.backend.domain.auth.ExternalLoginRepositoryPort;
import com.beeacademy.backend.infrastructure.auth.persistence.entity.ExternalLoginJpaEntity;
import com.beeacademy.backend.infrastructure.auth.persistence.repository.SpringDataExternalLoginRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class ExternalLoginRepositoryAdapter implements ExternalLoginRepositoryPort {

    private final SpringDataExternalLoginRepository springDataRepository;

    public ExternalLoginRepositoryAdapter(SpringDataExternalLoginRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Optional<ExternalLogin> findByProviderAndProviderUserId(String provider, String providerUserId) {
        return springDataRepository.findByProviderAndProviderUserId(provider.toUpperCase(), providerUserId)
                .map(this::toDomain);
    }

    @Override
    public Optional<ExternalLogin> findByUserIdAndProvider(UUID userId, String provider) {
        return springDataRepository.findByUserIdAndProvider(userId, provider.toUpperCase())
                .map(this::toDomain);
    }

    @Override
    public ExternalLogin save(ExternalLogin externalLogin) {
        ExternalLoginJpaEntity entity = toEntity(externalLogin);
        ExternalLoginJpaEntity saved = springDataRepository.save(entity);
        return toDomain(saved);
    }

    private ExternalLogin toDomain(ExternalLoginJpaEntity entity) {
        return new ExternalLogin(
                entity.getId(),
                entity.getUserId(),
                entity.getProvider(),
                entity.getProviderUserId(),
                entity.getEmail(),
                entity.getCreatedAt()
        );
    }

    private ExternalLoginJpaEntity toEntity(ExternalLogin domain) {
        return new ExternalLoginJpaEntity(
                domain.getId(),
                domain.getUserId(),
                domain.getProvider(),
                domain.getProviderUserId(),
                domain.getEmail(),
                domain.getCreatedAt()
        );
    }
}

