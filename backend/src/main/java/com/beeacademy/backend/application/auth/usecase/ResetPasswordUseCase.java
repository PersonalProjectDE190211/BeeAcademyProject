package com.beeacademy.backend.application.auth.usecase;

import com.beeacademy.backend.application.auth.dto.ResetPasswordCommand;
import com.beeacademy.backend.application.auth.service.OtpService;
import com.beeacademy.backend.domain.auth.PasswordHash;
import com.beeacademy.backend.domain.auth.Profile;
import com.beeacademy.backend.domain.auth.ProfileRepositoryPort;
import com.beeacademy.backend.domain.auth.RefreshTokenRepositoryPort;
import com.beeacademy.backend.domain.common.DomainException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Use case Đặt lại mật khẩu mới qua mã OTP (UC03).
 */
@Service
public class ResetPasswordUseCase {

    private final ProfileRepositoryPort profileRepository;
    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;

    public ResetPasswordUseCase(ProfileRepositoryPort profileRepository,
                                RefreshTokenRepositoryPort refreshTokenRepository,
                                OtpService otpService,
                                PasswordEncoder passwordEncoder) {
        this.profileRepository = profileRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.otpService = otpService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void execute(ResetPasswordCommand command) {
        if (command == null || command.email() == null || command.otpOrToken() == null || command.newPassword() == null) {
            throw new DomainException("INVALID_COMMAND", "Thông tin đặt lại mật khẩu không đầy đủ");
        }

        String email = command.email().trim().toLowerCase();
        boolean otpValid = otpService.verifyOtp("RESET_PWD:" + email, command.otpOrToken());
        if (!otpValid) {
            throw new DomainException("INVALID_OTP", "Mã xác thực OTP không chính xác hoặc đã hết hạn");
        }

        Profile profile = profileRepository.findByEmail(email)
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "Không tìm thấy người dùng với email này"));

        String encodedPassword = passwordEncoder.encode(command.newPassword());
        profile.updatePassword(PasswordHash.of(encodedPassword));
        profileRepository.save(profile);

        // Thu hồi toàn bộ Refresh Token cũ để bảo vệ tài khoản khi vừa đổi mật khẩu
        refreshTokenRepository.revokeAllByUserId(profile.getId(), Instant.now());
    }
}

