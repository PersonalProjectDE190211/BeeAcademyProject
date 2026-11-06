package com.beeacademy.backend.application.auth;

import com.beeacademy.backend.application.auth.dto.AuthTokenResult;
import com.beeacademy.backend.application.auth.usecase.AuthenticateUserUseCase;
import com.beeacademy.backend.application.auth.usecase.RefreshTokenUseCase;
import com.beeacademy.backend.domain.auth.*;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.platform.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử Use Case: Xoay vòng Refresh Token & Phát hiện chiếm đoạt phiên (UC02)")
class RefreshTokenUseCaseTest {

    @Mock
    private ProfileRepositoryPort profileRepository;

    @Mock
    private RefreshTokenRepositoryPort refreshTokenRepository;

    @Mock
    private JwtService jwtService;

    private RefreshTokenUseCase useCase;

    private static final String RAW_TOKEN = "a1b2c3d4e5f678901234567890abcdef1234567890abcdef1234567890abcdef";

    @BeforeEach
    void setUp() {
        useCase = new RefreshTokenUseCase(profileRepository, refreshTokenRepository, jwtService);
    }

    @Test
    @DisplayName("Xoay vòng token thành công: thu hồi token cũ, cấp token mới và Access Token mới")
    void testRefreshTokenSuccess() {
        UUID userId = UUID.randomUUID();
        String tokenHash = AuthenticateUserUseCase.hashSha256(RAW_TOKEN);

        RefreshToken token = RefreshToken.issue(userId, tokenHash, Duration.ofDays(7));
        Profile profile = Profile.createNew(
                userId,
                "student@beeacademy.edu.vn",
                PasswordHash.of("hash123"),
                UserRole.STUDENT,
                "Nguyen Van A"
        );

        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(token));
        when(profileRepository.findById(userId)).thenReturn(Optional.of(profile));
        when(jwtService.generateAccessToken(eq(userId), eq("student@beeacademy.edu.vn"), eq("student"), eq("Nguyen Van A")))
                .thenReturn("new.jwt.access.token");

        AuthTokenResult result = useCase.execute(RAW_TOKEN);

        assertNotNull(result);
        assertEquals("new.jwt.access.token", result.accessToken());
        assertNotNull(result.refreshToken());
        assertNotEquals(RAW_TOKEN, result.refreshToken()); // Đã sinh ra refresh token mới
        assertTrue(token.isRevoked()); // Token cũ đã bị đánh dấu revoked
        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class)); // Lưu token cũ đã rotate & lưu token mới
    }

    @Test
    @DisplayName("Phát hiện chiếm đoạt phiên (Token Reuse): Gửi lại token đã revoked -> Thu hồi toàn bộ phiên của User")
    void testTokenReuseTheftDetection() {
        UUID userId = UUID.randomUUID();
        String tokenHash = AuthenticateUserUseCase.hashSha256(RAW_TOKEN);

        RefreshToken token = RefreshToken.issue(userId, tokenHash, Duration.ofDays(7));
        // Đã bị rotate/revoke trước đó
        token.rotate("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", Instant.now());

        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(token));

        DomainException ex = assertThrows(DomainException.class, () -> useCase.execute(RAW_TOKEN));
        assertEquals("TOKEN_THEFT_DETECTED", ex.getErrorCode());

        // Kiểm tra hệ thống thu hồi toàn bộ phiên của người dùng này
        verify(refreshTokenRepository).revokeAllByUserId(eq(userId), any(Instant.class));
        verifyNoInteractions(jwtService);
    }

    @Test
    @DisplayName("Từ chối nếu người dùng bị Admin khóa tài khoản (Zero-lag Status Check)")
    void testBlockedUserCannotRefresh() {
        UUID userId = UUID.randomUUID();
        String tokenHash = AuthenticateUserUseCase.hashSha256(RAW_TOKEN);

        RefreshToken token = RefreshToken.issue(userId, tokenHash, Duration.ofDays(7));
        Profile profile = Profile.createNew(
                userId,
                "student@beeacademy.edu.vn",
                PasswordHash.of("hash123"),
                UserRole.STUDENT,
                "Nguyen Van A"
        );
        profile.block(); // Admin khóa tài khoản

        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(token));
        when(profileRepository.findById(userId)).thenReturn(Optional.of(profile));

        DomainException ex = assertThrows(DomainException.class, () -> useCase.execute(RAW_TOKEN));
        assertEquals("ACCOUNT_BLOCKED", ex.getErrorCode());
        assertTrue(token.isRevoked());
        verify(refreshTokenRepository).save(token);
    }
}
