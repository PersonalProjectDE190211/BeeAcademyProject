package com.beeacademy.backend.application.auth;

import com.beeacademy.backend.application.auth.dto.ChangePasswordCommand;
import com.beeacademy.backend.application.auth.dto.RegisterCommand;
import com.beeacademy.backend.application.auth.dto.UserProfileResult;
import com.beeacademy.backend.application.auth.usecase.ChangePasswordUseCase;
import com.beeacademy.backend.application.auth.usecase.RegisterUserUseCase;
import com.beeacademy.backend.domain.auth.*;
import com.beeacademy.backend.domain.common.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử Use Cases: Đăng ký & Đổi mật khẩu (UC01, UC05)")
class RegisterAndChangePasswordTest {

    @Mock
    private ProfileRepositoryPort profileRepository;

    @Mock
    private RefreshTokenRepositoryPort refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private RegisterUserUseCase registerUseCase;
    private ChangePasswordUseCase changePasswordUseCase;

    @BeforeEach
    void setUp() {
        registerUseCase = new RegisterUserUseCase(profileRepository, passwordEncoder);
        changePasswordUseCase = new ChangePasswordUseCase(profileRepository, refreshTokenRepository, passwordEncoder);
    }

    @Test
    @DisplayName("UC01 - Đăng ký thành công: mã hóa mật khẩu, lưu vào cơ sở dữ liệu")
    void testRegisterSuccess() {
        when(profileRepository.existsByEmail("newstudent@beeacademy.edu.vn")).thenReturn(false);
        when(passwordEncoder.encode("Pass123456")).thenReturn("$2a$12$EncodedHashMocked");
        when(profileRepository.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));

        RegisterCommand command = new RegisterCommand(
                "newstudent@beeacademy.edu.vn",
                "Pass123456",
                "Tran Thi B",
                UserRole.STUDENT
        );

        UserProfileResult result = registerUseCase.execute(command);
        assertNotNull(result);
        assertEquals("newstudent@beeacademy.edu.vn", result.email());
        assertEquals("student", result.role());
        assertEquals("Tran Thi B", result.fullName());
    }

    @Test
    @DisplayName("UC01 - Từ chối đăng ký nếu Email đã tồn tại trong hệ thống")
    void testRegisterDuplicateEmail() {
        when(profileRepository.existsByEmail("duplicate@beeacademy.edu.vn")).thenReturn(true);

        RegisterCommand command = new RegisterCommand(
                "duplicate@beeacademy.edu.vn",
                "Pass123456",
                "Tran Thi B",
                UserRole.STUDENT
        );

        DomainException ex = assertThrows(DomainException.class, () -> registerUseCase.execute(command));
        assertEquals("EMAIL_ALREADY_EXISTS", ex.getErrorCode());
        verify(profileRepository, never()).save(any());
    }

    @Test
    @DisplayName("UC05 - Đổi mật khẩu thành công: xác thực mật khẩu cũ, cập nhật băm mới, thu hồi token")
    void testChangePasswordSuccess() {
        UUID userId = UUID.randomUUID();
        Profile profile = Profile.createNew(
                userId,
                "student@beeacademy.edu.vn",
                PasswordHash.of("$2a$12$OldPasswordHash"),
                UserRole.STUDENT,
                "Tran Thi B"
        );

        when(profileRepository.findById(userId)).thenReturn(Optional.of(profile));
        when(passwordEncoder.matches("OldPassword123", "$2a$12$OldPasswordHash")).thenReturn(true);
        when(passwordEncoder.encode("NewPassword456")).thenReturn("$2a$12$NewPasswordHash");

        ChangePasswordCommand command = new ChangePasswordCommand("OldPassword123", "NewPassword456");
        changePasswordUseCase.execute(userId, command);

        verify(profileRepository).save(profile);
        verify(refreshTokenRepository).revokeAllByUserId(eq(userId), any(Instant.class));
    }

    @Test
    @DisplayName("UC05 - Đổi mật khẩu thất bại nếu mật khẩu cũ không khớp")
    void testChangePasswordWrongOldPassword() {
        UUID userId = UUID.randomUUID();
        Profile profile = Profile.createNew(
                userId,
                "student@beeacademy.edu.vn",
                PasswordHash.of("$2a$12$OldPasswordHash"),
                UserRole.STUDENT,
                "Tran Thi B"
        );

        when(profileRepository.findById(userId)).thenReturn(Optional.of(profile));
        when(passwordEncoder.matches("WrongOldPassword", "$2a$12$OldPasswordHash")).thenReturn(false);

        ChangePasswordCommand command = new ChangePasswordCommand("WrongOldPassword", "NewPassword456");
        DomainException ex = assertThrows(DomainException.class, () -> changePasswordUseCase.execute(userId, command));
        assertEquals("INVALID_OLD_PASSWORD", ex.getErrorCode());
        verify(refreshTokenRepository, never()).revokeAllByUserId(any(), any());
    }
}

