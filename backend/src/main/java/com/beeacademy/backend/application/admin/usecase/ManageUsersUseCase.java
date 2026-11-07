package com.beeacademy.backend.application.admin.usecase;

import com.beeacademy.backend.application.admin.dto.AdminUserDetailDto;
import com.beeacademy.backend.application.admin.dto.AdminUserSummaryDto;
import com.beeacademy.backend.domain.auth.LoginLockoutState;
import com.beeacademy.backend.domain.auth.Profile;
import com.beeacademy.backend.domain.auth.ProfileRepositoryPort;
import com.beeacademy.backend.domain.auth.RefreshTokenRepositoryPort;
import com.beeacademy.backend.domain.common.AuditEntityType;
import com.beeacademy.backend.domain.common.AuditLog;
import com.beeacademy.backend.domain.common.AuditLogRepositoryPort;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.common.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ManageUsersUseCase {

    private final ProfileRepositoryPort profileRepository;
    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final AuditLogRepositoryPort auditLogRepository;

    public ManageUsersUseCase(
            ProfileRepositoryPort profileRepository,
            RefreshTokenRepositoryPort refreshTokenRepository,
            AuditLogRepositoryPort auditLogRepository
    ) {
        this.profileRepository = profileRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public Page<AdminUserSummaryDto> searchUsers(String keyword, String role, String status, Pageable pageable) {
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        String r = (role != null && !role.isBlank()) ? role.trim().toLowerCase() : null;
        String s = (status != null && !status.isBlank()) ? status.trim().toLowerCase() : null;
        int page = pageable != null && pageable.isPaged() ? pageable.getPageNumber() : 0;
        int size = pageable != null && pageable.isPaged() ? pageable.getPageSize() : 20;

        PageResult<Profile> result = profileRepository.searchProfiles(r, s, kw, page, size);
        List<AdminUserSummaryDto> content = result.content().stream()
                .map(p -> new AdminUserSummaryDto(
                        p.getId(),
                        p.getEmail(),
                        p.getFullName(),
                        p.getRole().name(),
                        p.isBlocked() ? "LOCKED" : "ACTIVE",
                        p.getTeacherApprovalStatus().name(),
                        true,
                        p.getCreatedAt()
                ))
                .toList();

        return new PageImpl<>(content, PageRequest.of(result.page(), Math.max(1, result.size())), result.totalElements());
    }

    @Transactional(readOnly = true)
    public AdminUserDetailDto getUserDetail(UUID userId) {
        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "Không tìm thấy người dùng"));

        LoginLockoutState lockout = profile.getLockoutState();
        return new AdminUserDetailDto(
                profile.getId(),
                profile.getEmail(),
                profile.getFullName(),
                profile.getAvatarUrl(),
                profile.getPhone(),
                profile.getBio(),
                profile.getDateOfBirth(),
                profile.getRole().name(),
                profile.isBlocked() ? "LOCKED" : "ACTIVE",
                profile.getTeacherApprovalStatus().name(),
                true,
                lockout != null ? lockout.failedAttempts() : 0,
                lockout != null ? lockout.lockedUntil() : null,
                profile.isMustChangePassword(),
                profile.getCreatedByAdminId(),
                profile.getTeacherContactNote(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }

    @Transactional
    public void lockUser(UUID userId, String reason, UUID adminId) {
        Profile user = profileRepository.findById(userId)
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "Không tìm thấy người dùng"));

        if (user.getRole().isAdmin()) {
            throw new DomainException("CANNOT_LOCK_ADMIN", "Không được phép khóa tài khoản Quản trị viên");
        }

        user.block();
        profileRepository.save(user);
        refreshTokenRepository.revokeAllByUserId(userId, Instant.now());

        AuditLog audit = AuditLog.record(
                AuditEntityType.USER_ACCOUNT,
                userId,
                "LOCK",
                adminId,
                "ADMIN",
                reason != null && !reason.isBlank() ? reason.trim() : "Khóa tài khoản bởi Admin",
                "{\"status\":\"locked\"}"
        );
        auditLogRepository.save(audit);
    }

    @Transactional
    public void unlockUser(UUID userId, String reason, UUID adminId) {
        Profile user = profileRepository.findById(userId)
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "Không tìm thấy người dùng"));

        user.unblock();
        user.resetFailedLogin();
        profileRepository.save(user);

        AuditLog audit = AuditLog.record(
                AuditEntityType.USER_ACCOUNT,
                userId,
                "UNLOCK",
                adminId,
                "ADMIN",
                reason != null && !reason.isBlank() ? reason.trim() : "Mở khóa tài khoản bởi Admin",
                "{\"status\":\"active\"}"
        );
        auditLogRepository.save(audit);
    }
}
