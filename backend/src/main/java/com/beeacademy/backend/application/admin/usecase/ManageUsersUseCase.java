package com.beeacademy.backend.application.admin.usecase;

import com.beeacademy.backend.application.admin.dto.AdminUserDetailDto;
import com.beeacademy.backend.application.admin.dto.AdminUserSummaryDto;
import com.beeacademy.backend.domain.common.AuditEntityType;
import com.beeacademy.backend.domain.common.AuditLog;
import com.beeacademy.backend.domain.common.AuditLogRepositoryPort;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.infrastructure.auth.persistence.entity.ProfileJpaEntity;
import com.beeacademy.backend.infrastructure.auth.persistence.repository.SpringDataProfileRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class ManageUsersUseCase {

    private final SpringDataProfileRepository profileRepository;
    private final AuditLogRepositoryPort auditLogRepository;

    public ManageUsersUseCase(
            SpringDataProfileRepository profileRepository,
            AuditLogRepositoryPort auditLogRepository
    ) {
        this.profileRepository = profileRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public Page<AdminUserSummaryDto> searchUsers(String keyword, String role, String status, Pageable pageable) {
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        String r = (role != null && !role.isBlank()) ? role.trim() : null;
        String s = (status != null && !status.isBlank()) ? status.trim() : null;

        return profileRepository.searchProfiles(r, s, kw, pageable).map(p -> new AdminUserSummaryDto(
                p.getId(),
                p.getEmail(),
                p.getFullName(),
                p.getRole().toUpperCase(),
                p.getStatus().toUpperCase(),
                p.getTeacherApprovalStatus().toUpperCase(),
                p.isEmailVerified(),
                p.getCreatedAt()
        ));
    }

    @Transactional(readOnly = true)
    public AdminUserDetailDto getUserDetail(UUID userId) {
        ProfileJpaEntity entity = profileRepository.findById(userId)
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "Không tìm thấy người dùng"));

        return new AdminUserDetailDto(
                entity.getId(),
                entity.getEmail(),
                entity.getFullName(),
                entity.getAvatarUrl(),
                entity.getPhone(),
                entity.getBio(),
                null,
                entity.getRole().toUpperCase(),
                entity.getStatus().toUpperCase(),
                entity.getTeacherApprovalStatus().toUpperCase(),
                entity.isEmailVerified(),
                entity.getFailedLoginAttempts(),
                entity.getLockedUntil(),
                false,
                null,
                null,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    @Transactional
    public void lockUser(UUID userId, String reason, UUID adminId) {
        ProfileJpaEntity user = profileRepository.findById(userId)
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "Không tìm thấy người dùng"));

        if ("admin".equalsIgnoreCase(user.getRole())) {
            throw new DomainException("CANNOT_LOCK_ADMIN", "Không được phép khóa tài khoản Quản trị viên");
        }

        user.setStatus("locked");
        user.setUpdatedAt(Instant.now());
        profileRepository.save(user);

        AuditLog audit = AuditLog.record(
                AuditEntityType.USER_ACCOUNT,
                userId,
                "LOCK",
                adminId,
                "ADMIN",
                reason != null ? reason.trim() : "Khóa tài khoản bởi Admin",
                "{\"status\":\"locked\"}"
        );
        auditLogRepository.save(audit);
    }

    @Transactional
    public void unlockUser(UUID userId, String reason, UUID adminId) {
        ProfileJpaEntity user = profileRepository.findById(userId)
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "Không tìm thấy người dùng"));

        user.setStatus("active");
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setUpdatedAt(Instant.now());
        profileRepository.save(user);

        AuditLog audit = AuditLog.record(
                AuditEntityType.USER_ACCOUNT,
                userId,
                "UNLOCK",
                adminId,
                "ADMIN",
                reason != null ? reason.trim() : "Mở khóa tài khoản bởi Admin",
                "{\"status\":\"active\"}"
        );
        auditLogRepository.save(audit);
    }
}

