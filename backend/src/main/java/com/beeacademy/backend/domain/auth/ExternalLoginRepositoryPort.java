package com.beeacademy.backend.domain.auth;

import java.util.Optional;
import java.util.UUID;

/**
 * Port lưu trữ và tra cứu liên kết tài khoản định danh mạng xã hội.
 */
public interface ExternalLoginRepositoryPort {

    Optional<ExternalLogin> findByProviderAndProviderUserId(String provider, String providerUserId);

    Optional<ExternalLogin> findByUserIdAndProvider(UUID userId, String provider);

    ExternalLogin save(ExternalLogin externalLogin);
}

