package com.beeacademy.backend.application.admin;

import com.beeacademy.backend.application.admin.dto.AdminUserDetailDto;
import com.beeacademy.backend.application.admin.dto.AdminUserSummaryDto;
import com.beeacademy.backend.application.admin.usecase.ManageUsersUseCase;
import com.beeacademy.backend.domain.common.AuditLog;
import com.beeacademy.backend.domain.common.AuditLogRepositoryPort;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.infrastructure.auth.persistence.entity.ProfileJpaEntity;
import com.beeacademy.backend.infrastructure.auth.persistence.repository.SpringDataProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManageUsersUseCaseTest {

    @Mock
    private SpringDataProfileRepository profileRepository;

    @Mock
    private AuditLogRepositoryPort auditLogRepository;

    @InjectMocks
    private ManageUsersUseCase useCase;

    private UUID userId;
    private UUID adminId;
    private ProfileJpaEntity studentEntity;
    private ProfileJpaEntity adminEntity;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        adminId = UUID.randomUUID();

        studentEntity = new ProfileJpaEntity(
                userId,
                "student@bee.edu.vn",
                "hash",
                "Nguyễn Văn Học",
                null,
                null,
                null,
                "student",
                10,
                "active",
                "none",
                true,
                0,
                null,
                Instant.now(),
                Instant.now()
        );

        adminEntity = new ProfileJpaEntity(
                adminId,
                "admin@bee.edu.vn",
                "hash",
                "Quản trị viên",
                null,
                null,
                null,
                "admin",
                null,
                "active",
                "none",
                true,
                0,
                null,
                Instant.now(),
                Instant.now()
        );
    }

    @Test
    @DisplayName("searchUsers phân trang và tìm kiếm danh sách người dùng")
    void shouldSearchUsers() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(profileRepository.searchProfiles(null, null, "Học", pageable))
                .thenReturn(new PageImpl<>(List.of(studentEntity)));

        Page<AdminUserSummaryDto> page = useCase.searchUsers("Học", null, null, pageable);

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).fullName()).isEqualTo("Nguyễn Văn Học");
        assertThat(page.getContent().get(0).role()).isEqualTo("STUDENT");
    }

    @Test
    @DisplayName("getUserDetail trả về thông tin chi tiết người dùng")
    void shouldGetUserDetail() {
        when(profileRepository.findById(userId)).thenReturn(Optional.of(studentEntity));

        AdminUserDetailDto detail = useCase.getUserDetail(userId);

        assertThat(detail.id()).isEqualTo(userId);
        assertThat(detail.email()).isEqualTo("student@bee.edu.vn");
        assertThat(detail.status()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("lockUser khóa tài khoản và ghi audit log")
    void shouldLockUser() {
        when(profileRepository.findById(userId)).thenReturn(Optional.of(studentEntity));

        useCase.lockUser(userId, "Spam nội dung", adminId);

        assertThat(studentEntity.getStatus()).isEqualTo("locked");
        verify(profileRepository).save(studentEntity);
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("lockUser ném ngoại lệ khi cố khóa tài khoản Admin")
    void shouldThrowWhenLockingAdmin() {
        when(profileRepository.findById(adminId)).thenReturn(Optional.of(adminEntity));

        assertThatThrownBy(() -> useCase.lockUser(adminId, "Thử khóa admin", adminId))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Không được phép khóa tài khoản Quản trị viên");

        verify(profileRepository, never()).save(any());
    }

    @Test
    @DisplayName("unlockUser mở khóa tài khoản và reset số lần login thất bại")
    void shouldUnlockUser() {
        studentEntity.setStatus("locked");
        studentEntity.setFailedLoginAttempts(5);
        when(profileRepository.findById(userId)).thenReturn(Optional.of(studentEntity));

        useCase.unlockUser(userId, "Đã xác minh danh tính", adminId);

        assertThat(studentEntity.getStatus()).isEqualTo("active");
        assertThat(studentEntity.getFailedLoginAttempts()).isEqualTo(0);
        verify(profileRepository).save(studentEntity);
        verify(auditLogRepository).save(any(AuditLog.class));
    }
}

