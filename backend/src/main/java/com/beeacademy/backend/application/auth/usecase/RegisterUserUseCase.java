package com.beeacademy.backend.application.auth.usecase;

import com.beeacademy.backend.application.auth.dto.RegisterCommand;
import com.beeacademy.backend.application.auth.dto.UserProfileResult;
import com.beeacademy.backend.domain.auth.PasswordHash;
import com.beeacademy.backend.domain.auth.Profile;
import com.beeacademy.backend.domain.auth.ProfileRepositoryPort;
import com.beeacademy.backend.domain.common.DomainException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case thực hiện Đăng ký tài khoản người dùng mới (UC01).
 */
@Service
public class RegisterUserUseCase {

    private final ProfileRepositoryPort profileRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterUserUseCase(ProfileRepositoryPort profileRepository,
                               PasswordEncoder passwordEncoder) {
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserProfileResult execute(RegisterCommand command) {
        if (command == null) {
            throw new DomainException("INVALID_COMMAND", "Dữ liệu đăng ký không được rỗng");
        }

        String email = command.email().trim().toLowerCase();
        if (profileRepository.existsByEmail(email)) {
            throw new DomainException("EMAIL_ALREADY_EXISTS", "Email này đã được đăng ký trong hệ thống");
        }

        String encodedPassword = passwordEncoder.encode(command.password());
        Profile newProfile = Profile.createNew(
                UUID.randomUUID(),
                email,
                PasswordHash.of(encodedPassword),
                command.role(),
                command.fullName()
        );

        Profile saved = profileRepository.save(newProfile);
        return UserProfileResult.from(saved);
    }
}

