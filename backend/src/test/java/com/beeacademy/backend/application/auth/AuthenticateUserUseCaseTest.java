package com.beeacademy.backend.application.auth;

import com.beeacademy.backend.application.auth.dto.AuthTokenResult;
import com.beeacademy.backend.application.auth.dto.LoginCommand;
import com.beeacademy.backend.application.auth.usecase.AuthenticateUserUseCase;
import com.beeacademy.backend.domain.auth.*;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.platform.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
@DisplayName("Kiểm thử Use Case: Đăng nhập & Xác thực người dùng (UC02)")
class AuthenticateUserUseCaseTest {

    @Mock
    private ProfileRepositoryPort profileRepository;

    @Mock
    private RefreshTokenRepositoryPort refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AuthenticateUserUseCase useCase;

    private static final String RAW_PASSWORD = "Password@123";
    private static final String ENCODED_HASH = "$2a$12$DummyEncodedHashForTesting123456789";

    @BeforeEach
    void setUp() {
        useCase = new AuthenticateUserUseCase(
                profileRepository,
                refreshTokenRepository,
                passwordEncoder,
                jwtService
        );
    }

    @Test
    @DisplayName("Đăng nhập thành công: reset lockout, cấp phát Access Token và Refresh Token SHA-256")
    void testLoginSuccess() {
        UUID userId = UUID.randomUUID();
        Profile profile = Profile.createNew(
                userId,
                "student@beeacademy.edu.vn",
                PasswordHash.of(ENCODED_HASH),
                UserRole.STUDENT,
                "Nguyen Van A"
        );

        when(profileRepository.findByEmail("student@beeacademy.edu.vn")).thenReturn(Optional.of(profile));
        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_HASH)).thenReturn(true);
        when(jwtService.generateAccessToken(eq(userId), eq("student@beeacademy.edu.vn"), eq("student"), eq("Nguyen Van A")))
                .thenReturn("mocked.jwt.access.token");
        when(profileRepository.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));

        LoginCommand command = new LoginCommand("student@beeacademy.edu.vn", RAW_PASSWORD, "Chrome/Windows", "127.0.0.1");
        AuthTokenResult result = useCase.execute(command);

        assertNotNull(result);
        assertEquals("mocked.jwt.access.token", result.accessToken());
        assertNotNull(result.refreshToken());
        assertEquals(64, result.refreshToken().length()); // 64 hex characters
        assertEquals("Nguyen Van A", result.user().fullName());

        // Kiểm tra Refresh Token lưu trong DB là bản băm SHA-256
        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(tokenCaptor.capture());
        RefreshToken savedToken = tokenCaptor.getValue();
        assertEquals(userId, savedToken.getUserId());
        assertEquals(64, savedToken.getTokenHash().length());
        assertNotEquals(result.refreshToken(), savedToken.getTokenHash()); // Tuyệt đối không lưu raw token
    }

    @Test
    @DisplayName("Đăng nhập sai mật khẩu: tăng số lần sai, ném ngoại lệ INVALID_CREDENTIALS")
    void testLoginWrongPassword() {
        UUID userId = UUID.randomUUID();
        Profile profile = Profile.createNew(
                userId,
                "student@beeacademy.edu.vn",
                PasswordHash.of(ENCODED_HASH),
                UserRole.STUDENT,
                "Nguyen Van A"
        );

        when(profileRepository.findByEmail("student@beeacademy.edu.vn")).thenReturn(Optional.of(profile));
        when(passwordEncoder.matches("WrongPassword", ENCODED_HASH)).thenReturn(false);

        LoginCommand command = new LoginCommand("student@beeacademy.edu.vn", "WrongPassword", "Chrome", "127.0.0.1");

        DomainException ex = assertThrows(DomainException.class, () -> useCase.execute(command));
        assertEquals("INVALID_CREDENTIALS", ex.getErrorCode());
        verify(profileRepository).save(any(Profile.class));
        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    @DisplayName("Khóa tạm thời 15 phút sau 5 lần nhập sai liên tiếp (REQ-AUTH-002)")
    void testLockoutAfterFiveFailedAttempts() {
        UUID userId = UUID.randomUUID();
        Profile profile = Profile.createNew(
                userId,
                "student@beeacademy.edu.vn",
                PasswordHash.of(ENCODED_HASH),
                UserRole.STUDENT,
                "Nguyen Van A"
        );

        // Giả lập đã nhập sai 4 lần trước đó
        Instant now = Instant.now();
        for (int i = 0; i < 4; i++) {
            profile.registerFailedLogin(now);
        }

        when(profileRepository.findByEmail("student@beeacademy.edu.vn")).thenReturn(Optional.of(profile));
        when(passwordEncoder.matches("WrongFifthTime", ENCODED_HASH)).thenReturn(false);

        LoginCommand command = new LoginCommand("student@beeacademy.edu.vn", "WrongFifthTime", "Chrome", "127.0.0.1");

        // Lần thứ 5 sai -> ném ACCOUNT_TEMPORARILY_LOCKED
        DomainException ex = assertThrows(DomainException.class, () -> useCase.execute(command));
        assertEquals("ACCOUNT_TEMPORARILY_LOCKED", ex.getErrorCode());
        assertTrue(profile.isTemporarilyLocked(now));
    }

    @Test
    @DisplayName("Từ chối đăng nhập nếu tài khoản bị Admin khóa vĩnh viễn (isBlocked)")
    void testBlockedAccountCannotLogin() {
        UUID userId = UUID.randomUUID();
        Profile profile = Profile.createNew(
                userId,
                "student@beeacademy.edu.vn",
                PasswordHash.of(ENCODED_HASH),
                UserRole.STUDENT,
                "Nguyen Van A"
        );
        profile.block();

        when(profileRepository.findByEmail("student@beeacademy.edu.vn")).thenReturn(Optional.of(profile));

        LoginCommand command = new LoginCommand("student@beeacademy.edu.vn", RAW_PASSWORD, "Chrome", "127.0.0.1");

        DomainException ex = assertThrows(DomainException.class, () -> useCase.execute(command));
        assertEquals("ACCOUNT_BLOCKED", ex.getErrorCode());
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(refreshTokenRepository);
    }
}
