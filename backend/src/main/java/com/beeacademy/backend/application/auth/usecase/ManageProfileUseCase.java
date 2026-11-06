package com.beeacademy.backend.application.auth.usecase;

import com.beeacademy.backend.application.auth.dto.UpdateProfileCommand;
import com.beeacademy.backend.application.auth.dto.UserProfileResult;
import com.beeacademy.backend.domain.auth.Profile;
import com.beeacademy.backend.domain.auth.ProfileRepositoryPort;
import com.beeacademy.backend.domain.common.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case Quản lý thông tin hồ sơ người dùng (UC04).
 */
@Service
public class ManageProfileUseCase {

    private final ProfileRepositoryPort profileRepository;

    public ManageProfileUseCase(ProfileRepositoryPort profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Transactional(readOnly = true)
    public UserProfileResult getProfile(UUID userId) {
        if (userId == null) {
            throw new DomainException("INVALID_USER_ID", "ID người dùng không được rỗng");
        }
        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "Không tìm thấy thông tin người dùng"));
        return UserProfileResult.from(profile);
    }

    @Transactional
    public UserProfileResult updateProfile(UUID userId, UpdateProfileCommand command) {
        if (userId == null || command == null) {
            throw new DomainException("INVALID_COMMAND", "Dữ liệu cập nhật hồ sơ không hợp lệ");
        }

        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "Không tìm thấy thông tin người dùng"));

        profile.updatePersonalInfo(command.fullName(), command.phone(), command.bio(), null);
        Profile saved = profileRepository.save(profile);
        return UserProfileResult.from(saved);
    }

    @Transactional
    public UserProfileResult updateAvatar(UUID userId, String avatarUrl) {
        if (userId == null) {
            throw new DomainException("INVALID_USER_ID", "ID người dùng không được rỗng");
        }

        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "Không tìm thấy thông tin người dùng"));

        profile.changeAvatar(avatarUrl);
        Profile saved = profileRepository.save(profile);
        return UserProfileResult.from(saved);
    }
}

