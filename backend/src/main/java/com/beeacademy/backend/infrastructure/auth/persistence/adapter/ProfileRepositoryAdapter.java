package com.beeacademy.backend.infrastructure.auth.persistence.adapter;

import com.beeacademy.backend.domain.auth.*;
import com.beeacademy.backend.infrastructure.auth.persistence.entity.ProfileJpaEntity;
import com.beeacademy.backend.infrastructure.auth.persistence.repository.SpringDataProfileRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter triển khai Domain Port {@link ProfileRepositoryPort} bằng Spring Data JPA.
 */
@Component
public class ProfileRepositoryAdapter implements ProfileRepositoryPort {

    private final SpringDataProfileRepository repository;

    public ProfileRepositoryAdapter(SpringDataProfileRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Profile> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Profile> findByEmail(String email) {
        return repository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public Profile save(Profile profile) {
        ProfileJpaEntity entity = toEntity(profile);
        ProfileJpaEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    private Profile toDomain(ProfileJpaEntity entity) {
        boolean isBlocked = "locked".equalsIgnoreCase(entity.getStatus()) || "suspended".equalsIgnoreCase(entity.getStatus());
        LoginLockoutState lockoutState = new LoginLockoutState(
                entity.getFailedLoginAttempts(),
                null,
                entity.getLockedUntil()
        );

        return Profile.rehydrate(
                entity.getId(),
                entity.getEmail(),
                PasswordHash.of(entity.getPasswordHash()),
                UserRole.fromDbValue(entity.getRole()),
                entity.getFullName(),
                entity.getAvatarUrl(),
                entity.getPhone(),
                entity.getBio(),
                null, // dateOfBirth
                TeacherApprovalStatus.fromDbValue(entity.getTeacherApprovalStatus()),
                false, // mustChangePassword
                null,  // createdByAdminId
                null,  // teacherContactNote
                isBlocked,
                lockoutState,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private ProfileJpaEntity toEntity(Profile domain) {
        String status = domain.isBlocked() ? "locked" : "active";
        int failedAttempts = domain.getLockoutState() != null ? domain.getLockoutState().failedAttempts() : 0;
        var lockedUntil = domain.getLockoutState() != null ? domain.getLockoutState().lockedUntil() : null;

        return new ProfileJpaEntity(
                domain.getId(),
                domain.getEmail(),
                domain.getPasswordHash().value(),
                domain.getFullName(),
                domain.getAvatarUrl(),
                domain.getPhone(),
                domain.getBio(),
                domain.getRole().getDbValue(),
                null, // grade
                status,
                domain.getTeacherApprovalStatus() != null ? domain.getTeacherApprovalStatus().name().toLowerCase() : "none",
                false, // emailVerified
                failedAttempts,
                lockedUntil,
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }
}
