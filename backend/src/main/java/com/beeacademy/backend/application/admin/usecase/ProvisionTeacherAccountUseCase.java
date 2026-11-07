package com.beeacademy.backend.application.admin.usecase;

import com.beeacademy.backend.application.admin.dto.AdminUserDetailDto;
import com.beeacademy.backend.application.admin.dto.ProvisionTeacherCommand;
import com.beeacademy.backend.domain.common.AuditEntityType;
import com.beeacademy.backend.domain.common.AuditLog;
import com.beeacademy.backend.domain.common.AuditLogRepositoryPort;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.infrastructure.auth.persistence.entity.ProfileJpaEntity;
import com.beeacademy.backend.infrastructure.auth.persistence.repository.SpringDataProfileRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class ProvisionTeacherAccountUseCase {

    private final SpringDataProfileRepository profileRepository;
    private final AuditLogRepositoryPort auditLogRepository;
    private final PasswordEncoder passwordEncoder;

    public ProvisionTeacherAccountUseCase(
            SpringDataProfileRepository profileRepository,
            AuditLogRepositoryPort auditLogRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.profileRepository = profileRepository;
        this.auditLogRepository = auditLogRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void approveTeacher(UUID teacherId, UUID adminId) {
        ProfileJpaEntity user = profileRepository.findById(teacherId)
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "Không tìm thấy người dùng"));

        if (!"teacher".equalsIgnoreCase(user.getRole())) {
            throw new DomainException("INVALID_ROLE", "Chỉ tài khoản giáo viên mới có thể phê duyệt tư cách giảng dạy");
        }

        user.setTeacherApprovalStatus("approved");
        user.setUpdatedAt(Instant.now());
        profileRepository.save(user);

        AuditLog audit = AuditLog.record(
                AuditEntityType.USER_ACCOUNT,
                teacherId,
                "TEACHER_APPROVAL",
                adminId,
                "ADMIN",
                "Phê duyệt tư cách giáo viên giảng dạy trên BeeAcademy",
                "{\"teacherApprovalStatus\":\"approved\"}"
        );
        auditLogRepository.save(audit);
    }

    @Transactional
    public void rejectTeacher(UUID teacherId, String reason, UUID adminId) {
        ProfileJpaEntity user = profileRepository.findById(teacherId)
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "Không tìm thấy người dùng"));

        user.setTeacherApprovalStatus("rejected");
        user.setUpdatedAt(Instant.now());
        profileRepository.save(user);

        AuditLog audit = AuditLog.record(
                AuditEntityType.USER_ACCOUNT,
                teacherId,
                "TEACHER_REJECTION",
                adminId,
                "ADMIN",
                reason != null ? reason.trim() : "Từ chối hồ sơ giáo viên",
                "{\"teacherApprovalStatus\":\"rejected\"}"
        );
        auditLogRepository.save(audit);
    }

    @Transactional
    public AdminUserDetailDto provisionTeacherAccount(ProvisionTeacherCommand command, UUID adminId) {
        if (command == null || command.email() == null || command.email().isBlank()) {
            throw new DomainException("INVALID_COMMAND", "Email không được để trống");
        }
        if (command.temporaryPassword() == null || command.temporaryPassword().length() < 6) {
            throw new DomainException("INVALID_PASSWORD", "Mật khẩu tạm phải có ít nhất 6 ký tự");
        }
        if (command.fullName() == null || command.fullName().isBlank()) {
            throw new DomainException("INVALID_FULL_NAME", "Họ tên giáo viên không được để trống");
        }

        String email = command.email().trim().toLowerCase();
        if (profileRepository.existsByEmail(email)) {
            throw new DomainException("EMAIL_ALREADY_EXISTS", "Email này đã được sử dụng trong hệ thống");
        }

        UUID teacherId = UUID.randomUUID();
        ProfileJpaEntity entity = new ProfileJpaEntity(
                teacherId,
                email,
                passwordEncoder.encode(command.temporaryPassword()),
                command.fullName().trim(),
                null,
                null,
                null,
                "teacher",
                null,
                "active",
                "approved",
                true,
                0,
                null,
                Instant.now(),
                Instant.now()
        );

        ProfileJpaEntity saved = profileRepository.save(entity);

        AuditLog audit = AuditLog.record(
                AuditEntityType.USER_ACCOUNT,
                teacherId,
                "PROVISION_TEACHER",
                adminId,
                "ADMIN",
                command.contactNote() != null ? command.contactNote().trim() : "Cấp tài khoản giáo viên trực tiếp bởi Admin",
                "{\"email\":\"" + email + "\"}"
        );
        auditLogRepository.save(audit);

        return new AdminUserDetailDto(
                saved.getId(),
                saved.getEmail(),
                saved.getFullName(),
                saved.getAvatarUrl(),
                saved.getPhone(),
                saved.getBio(),
                null,
                saved.getRole().toUpperCase(),
                saved.getStatus().toUpperCase(),
                saved.getTeacherApprovalStatus().toUpperCase(),
                saved.isEmailVerified(),
                saved.getFailedLoginAttempts(),
                saved.getLockedUntil(),
                true,
                adminId,
                command.contactNote(),
                saved.getCreatedAt(),
                saved.getUpdatedAt()
        );
    }
}
