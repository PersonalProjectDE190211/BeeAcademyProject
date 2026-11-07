package com.beeacademy.backend.application.admin;

import com.beeacademy.backend.application.admin.dto.AdminUserDetailDto;
import com.beeacademy.backend.application.admin.dto.AdminUserSummaryDto;
import com.beeacademy.backend.application.admin.usecase.ManageUsersUseCase;
import com.beeacademy.backend.domain.auth.*;
import com.beeacademy.backend.domain.common.AuditLog;
import com.beeacademy.backend.domain.common.AuditLogRepositoryPort;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.common.PageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManageUsersUseCaseTest {

    @Mock
    private ProfileRepositoryPort profileRepository;

    @Mock
    private RefreshTokenRepositoryPort refreshTokenRepository;

    @Mock
    private AuditLogRepositoryPort auditLogRepository;

    @InjectMocks
    private ManageUsersUseCase useCase;

    private UUID userId;
    private UUID adminId;
    private Profile studentProfile;
    private Profile adminProfile;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        adminId = UUID.randomUUID();

        studentProfile = Profile.rehydrate(
                userId,
                "student@bee.edu.vn",
                PasswordHash.of("$2a$10$hash"),
                UserRole.STUDENT,
                "Nguyễn Văn Học",
                null,
                null,
                null,
                null,
                TeacherApprovalStatus.NONE,
                false,
                null,
                null,
                false,
                LoginLockoutState.initial(),
                Instant.now(),
                Instant.now()
        );

        adminProfile = Profile.rehydrate(
                adminId,
                "admin@bee.edu.vn",
                PasswordHash.of("$2a$10$hash"),
                UserRole.ADMIN,
                "Quản trị viên",
                null,
                null,
                null,
                null,
                TeacherApprovalStatus.NONE,
                false,
                null,
                null,
                false,
                LoginLockoutState.initial(),
                Instant.now(),
                Instant.now()
        );
    }

    @Test
    @DisplayName("searchUsers phân trang và tìm kiếm danh sách người dùng qua ProfileRepositoryPort")
    void shouldSearchUsers() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(profileRepository.searchProfiles(null, null, "Học", 0, 10))
                .thenReturn(new PageResult<>(List.of(studentProfile), 0, 10, 1));

        Page<AdminUserSummaryDto> page = useCase.searchUsers("Học", null, null, pageable);

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).fullName()).isEqualTo("Nguyễn Văn Học");
        assertThat(page.getContent().get(0).role()).isEqualTo("STUDENT");
    }

    @Test
    @DisplayName("getUserDetail trả về thông tin chi tiết người dùng")
    void shouldGetUserDetail() {
        when(profileRepository.findById(userId)).thenReturn(Optional.of(studentProfile));

        AdminUserDetailDto detail = useCase.getUserDetail(userId);

        assertThat(detail.id()).isEqualTo(userId);
        assertThat(detail.email()).isEqualTo("student@bee.edu.vn");
        assertThat(detail.status()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("lockUser khóa tài khoản, thu hồi refresh token và ghi audit log")
    void shouldLockUserAndRevokeRefreshTokens() {
        when(profileRepository.findById(userId)).thenReturn(Optional.of(studentProfile));

        useCase.lockUser(userId, "Spam nội dung", adminId);

        assertThat(studentProfile.isBlocked()).isTrue();
        verify(profileRepository).save(studentProfile);
        verify(refreshTokenRepository).revokeAllByUserId(eq(userId), any(Instant.class));
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("lockUser ném ngoại lệ khi cố khóa tài khoản Admin")
    void shouldThrowWhenLockingAdmin() {
        when(profileRepository.findById(adminId)).thenReturn(Optional.of(adminProfile));

        assertThatThrownBy(() -> useCase.lockUser(adminId, "Thử khóa admin", adminId))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Không được phép khóa tài khoản Quản trị viên");

        verify(profileRepository, never()).save(any());
        verify(refreshTokenRepository, never()).revokeAllByUserId(any(), any());
    }

    @Test
    @DisplayName("unlockUser mở khóa tài khoản và reset số lần login thất bại")
    void shouldUnlockUser() {
        Profile lockedStudent = Profile.rehydrate(
                userId,
                "student@bee.edu.vn",
                PasswordHash.of("$2a$10$hash"),
                UserRole.STUDENT,
                "Nguyễn Văn Học",
                null,
                null,
                null,
                null,
                TeacherApprovalStatus.NONE,
                false,
                null,
                null,
                true,
                LoginLockoutState.of(5, Instant.now(), Instant.now().plusSeconds(600)),
                Instant.now(),
                Instant.now()
        );
        when(profileRepository.findById(userId)).thenReturn(Optional.of(lockedStudent));

        useCase.unlockUser(userId, "Đã xác minh danh tính", adminId);

        assertThat(lockedStudent.isBlocked()).isFalse();
        assertThat(lockedStudent.getLockoutState().failedAttempts()).isEqualTo(0);
        verify(profileRepository).save(lockedStudent);
        verify(auditLogRepository).save(any(AuditLog.class));
    }
}
