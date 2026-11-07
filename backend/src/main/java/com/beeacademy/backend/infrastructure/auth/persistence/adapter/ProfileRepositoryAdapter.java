package com.beeacademy.backend.infrastructure.auth.persistence.adapter;

import com.beeacademy.backend.domain.auth.*;
import com.beeacademy.backend.domain.common.PageResult;
import com.beeacademy.backend.infrastructure.auth.persistence.entity.ProfileJpaEntity;
import com.beeacademy.backend.infrastructure.auth.persistence.repository.SpringDataProfileRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
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
        Optional<ProfileJpaEntity> existing = repository.findById(profile.getId());
        boolean emailVerified = existing
                .map(ProfileJpaEntity::isEmailVerified)
                .orElse(profile.getTeacherApprovalStatus() == TeacherApprovalStatus.APPROVED);
        Integer grade = existing.map(ProfileJpaEntity::getGrade).orElse(null);

        ProfileJpaEntity entity = toEntity(profile, grade, emailVerified);
        ProfileJpaEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public PageResult<Profile> searchProfiles(String role, String status, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        Page<ProfileJpaEntity> entityPage = repository.searchProfiles(role, status, keyword, pageable);
        List<Profile> content = entityPage.getContent().stream()
                .map(this::toDomain)
                .toList();
        return new PageResult<>(content, pageable.getPageNumber(), pageable.getPageSize(), entityPage.getTotalElements());
    }

    private Profile toDomain(ProfileJpaEntity entity) {
        boolean isBlocked = "locked".equalsIgnoreCase(entity.getStatus())
                || "suspended".equalsIgnoreCase(entity.getStatus());
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
                null,
                TeacherApprovalStatus.fromDbValue(entity.getTeacherApprovalStatus()),
                false,
                null,
                null,
                isBlocked,
                lockoutState,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private ProfileJpaEntity toEntity(Profile domain, Integer grade, boolean emailVerified) {
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
                grade,
                status,
                domain.getTeacherApprovalStatus() != null
                        ? domain.getTeacherApprovalStatus().name().toLowerCase()
                        : "none",
                emailVerified,
                failedAttempts,
                lockedUntil,
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }
}
