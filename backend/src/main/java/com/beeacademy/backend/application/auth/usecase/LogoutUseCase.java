package com.beeacademy.backend.application.auth.usecase;

import com.beeacademy.backend.domain.auth.RefreshToken;
import com.beeacademy.backend.domain.auth.RefreshTokenRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * Use case thực hiện Đăng xuất người dùng (UC02).
 * Thu hồi Refresh Token hiện tại để không thể tiếp tục sử dụng.
 */
@Service
public class LogoutUseCase {

    private final RefreshTokenRepositoryPort refreshTokenRepository;

    public LogoutUseCase(RefreshTokenRepositoryPort refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public void execute(String rawRefreshToken) {
        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            String tokenHash = AuthenticateUserUseCase.hashSha256(rawRefreshToken.trim());
            Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByTokenHash(tokenHash);
            if (tokenOpt.isPresent()) {
                RefreshToken token = tokenOpt.get();
                token.revoke(Instant.now());
                refreshTokenRepository.save(token);
            }
        }
    }
}

