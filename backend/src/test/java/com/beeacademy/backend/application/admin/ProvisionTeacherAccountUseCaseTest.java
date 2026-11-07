package com.beeacademy.backend.application.admin;

import com.beeacademy.backend.application.admin.dto.AdminUserDetailDto;
import com.beeacademy.backend.application.admin.dto.ProvisionTeacherCommand;
import com.beeacademy.backend.application.admin.usecase.ProvisionTeacherAccountUseCase;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProvisionTeacherAccountUseCaseTest {

    @Mock
    private SpringDataProfileRepository profileRepository;

    @Mock
    private AuditLogRepositoryPort auditLogRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ProvisionTeacherAccountUseCase useCase;

    private UUID teacherId;
    private UUID adminId;
    private ProfileJpaEntity teacherEntity;

    @BeforeEach
    void setUp() {
        teacherId = UUID.randomUUID();
        adminId = UUID.randomUUID();

        teacherEntity = new ProfileJpaEntity(
                teacherId,
                "teacher@bee.edu.vn",
                "hash",
                "Thầy Lê Văn Toán",
                null,
                null,
                null,
                "teacher",
                null,
                "active",
                "pending",
                true,
                0,
                null,
                Instant.now(),
                Instant.now()
        );
    }

    @Test
    @DisplayName("approveTeacher phê duyệt tư cách giáo viên và lưu audit log")
    void shouldApproveTeacher() {
        when(profileRepository.findById(teacherId)).thenReturn(Optional.of(teacherEntity));

        useCase.approveTeacher(teacherId, adminId);

        assertThat(teacherEntity.getTeacherApprovalStatus()).isEqualTo("approved");
        verify(profileRepository).save(teacherEntity);
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("rejectTeacher từ chối hồ sơ giáo viên")
    void shouldRejectTeacher() {
        when(profileRepository.findById(teacherId)).thenReturn(Optional.of(teacherEntity));

        useCase.rejectTeacher(teacherId, "Thiếu chứng chỉ sư phạm", adminId);

        assertThat(teacherEntity.getTeacherApprovalStatus()).isEqualTo("rejected");
        verify(profileRepository).save(teacherEntity);
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("provisionTeacherAccount tạo tài khoản giáo viên mới với mật khẩu mã hóa")
    void shouldProvisionTeacherAccount() {
        ProvisionTeacherCommand cmd = new ProvisionTeacherCommand(
                "newteacher@bee.edu.vn",
                "Temp@123456",
                "Cô Trần Thị Lý",
                "Tuyển dụng qua phỏng vấn đợt 1"
        );

        when(profileRepository.existsByEmail("newteacher@bee.edu.vn")).thenReturn(false);
        when(passwordEncoder.encode("Temp@123456")).thenReturn("encodedHash123");
        when(profileRepository.save(any(ProfileJpaEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        AdminUserDetailDto result = useCase.provisionTeacherAccount(cmd, adminId);

        assertThat(result.email()).isEqualTo("newteacher@bee.edu.vn");
        assertThat(result.fullName()).isEqualTo("Cô Trần Thị Lý");
        assertThat(result.role()).isEqualTo("TEACHER");
        assertThat(result.teacherApprovalStatus()).isEqualTo("APPROVED");
        verify(profileRepository).save(any(ProfileJpaEntity.class));
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("provisionTeacherAccount ném ngoại lệ khi email đã tồn tại")
    void shouldThrowWhenEmailAlreadyExists() {
        ProvisionTeacherCommand cmd = new ProvisionTeacherCommand(
                "existing@bee.edu.vn",
                "Temp@123456",
                "Cô Giáo",
                null
        );
        when(profileRepository.existsByEmail("existing@bee.edu.vn")).thenReturn(true);

        assertThatThrownBy(() -> useCase.provisionTeacherAccount(cmd, adminId))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Email này đã được sử dụng");
    }
}

