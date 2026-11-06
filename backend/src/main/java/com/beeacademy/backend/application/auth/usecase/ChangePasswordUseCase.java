package com.beeacademy.backend.application.auth.usecase;

import com.beeacademy.backend.application.auth.dto.ChangePasswordCommand;
import com.beeacademy.backend.domain.auth.PasswordHash;
import com.beeacademy.backend.domain.auth.Profile;
import com.beeacademy.backend.domain.auth.ProfileRepositoryPort;
import com.beeacademy.backend.domain.auth.RefreshTokenRepositoryPort;
import com.beeacademy.backend.domain.common.DomainException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Use case Đổi mật khẩu chủ động khi đã đăng nhập (UC05).
 */
@Service
public class ChangePasswordUseCase {

    private final ProfileRepositoryPort profileRepository;
    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public ChangePasswordUseCase(ProfileRepositoryPort profileRepository,
                                 RefreshTokenRepositoryPort refreshTokenRepository,
                                 PasswordEncoder passwordEncoder) {
        this.profileRepository = profileRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void execute(UUID userId, ChangePasswordCommand command) {
        if (userId == null || command == null) {
            throw new DomainException("INVALID_COMMAND", "Thông tin đổi mật khẩu không hợp lệ");
        }

        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "Không tìm thấy thông tin người dùng"));

        boolean oldPasswordMatches = passwordEncoder.matches(
                command.oldPassword(),
                profile.getPasswordHash().value()
        );

        if (!oldPasswordMatches) {
            throw new DomainException("INVALID_OLD_PASSWORD", "Mật khẩu cũ không chính xác");
        }

        String encodedNewPassword = passwordEncoder.encode(command.newPassword());
        profile.updatePassword(PasswordHash.of(encodedNewPassword));
        profileRepository.save(profile);

        // Thu hồi toàn bộ Refresh Token của tài khoản này
        refreshTokenRepository.revokeAllByUserId(userId, Instant.now());
    }
}
