package com.beeacademy.backend.application.auth;

import com.beeacademy.backend.application.auth.dto.AuthTokenResult;
import com.beeacademy.backend.application.auth.dto.GoogleAuthCommand;
import com.beeacademy.backend.application.auth.usecase.AuthenticateWithGoogleUseCase;
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
@DisplayName("Kiểm thử Use Case: Đăng nhập & Đăng ký bằng Google OAuth2 (UC02-Google)")
class AuthenticateWithGoogleUseCaseTest {

    @Mock
    private GoogleTokenVerifierPort googleTokenVerifier;

    @Mock
    private ExternalLoginRepositoryPort externalLoginRepository;

    @Mock
    private ProfileRepositoryPort profileRepository;

    @Mock
    private RefreshTokenRepositoryPort refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AuthenticateWithGoogleUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new AuthenticateWithGoogleUseCase(
                googleTokenVerifier,
                externalLoginRepository,
                profileRepository,
                refreshTokenRepository,
                passwordEncoder,
                jwtService
        );
    }

    @Test
    @DisplayName("Đăng nhập thành công khi tài khoản Google đã liên kết từ trước bằng sub")
    void testGoogleLogin_ExistingLinkedAccount_Success() {
        String token = "valid-google-id-token";
        String sub = "google-sub-123456789";
        String email = "student@gmail.com";
        UUID userId = UUID.randomUUID();

        GoogleUserClaims claims = new GoogleUserClaims(
                sub, email, true, "Nguyen Van A", "https://avatar.url/photo.jpg",
                "accounts.google.com", "bee-client-id"
        );
        when(googleTokenVerifier.verify(token)).thenReturn(claims);

        ExternalLogin externalLogin = ExternalLogin.create(userId, "GOOGLE", sub, email);
        when(externalLoginRepository.findByProviderAndProviderUserId("GOOGLE", sub))
                .thenReturn(Optional.of(externalLogin));

        Profile existingProfile = Profile.createNew(
                userId, email, PasswordHash.of("$2a$12$DummyHash"), UserRole.STUDENT, "Nguyen Van A"
        );
        when(profileRepository.findById(userId)).thenReturn(Optional.of(existingProfile));
        when(profileRepository.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));

        when(jwtService.generateAccessToken(eq(userId), eq(email), eq("student"), anyString()))
                .thenReturn("mock-access-token");

        GoogleAuthCommand command = new GoogleAuthCommand(token, null);
        AuthTokenResult result = useCase.execute(command);

        assertNotNull(result);
        assertEquals("mock-access-token", result.accessToken());
        assertNotNull(result.refreshToken());
        assertEquals(email, result.user().email());
        assertEquals("student", result.user().role());

        verify(refreshTokenRepository).save(any(RefreshToken.class));
        verify(externalLoginRepository, never()).save(any());
    }

    @Test
    @DisplayName("Tự động liên kết an toàn khi email đã tồn tại và email_verified = true")
    void testGoogleLogin_ExistingEmailUnlinked_AutoLinksAccount() {
        String token = "valid-google-id-token";
        String sub = "google-sub-987654321";
        String email = "existing@beeacademy.edu.vn";
        UUID userId = UUID.randomUUID();

        GoogleUserClaims claims = new GoogleUserClaims(
                sub, email, true, "Existing User", "https://avatar.url/new.jpg",
                "https://accounts.google.com", "bee-client-id"
        );
        when(googleTokenVerifier.verify(token)).thenReturn(claims);
        when(externalLoginRepository.findByProviderAndProviderUserId("GOOGLE", sub))
                .thenReturn(Optional.empty());

        Profile existingProfile = Profile.createNew(
                userId, email, PasswordHash.of("$2a$12$OldHash"), UserRole.STUDENT, "Old Full Name"
        );
        when(profileRepository.findByEmail(email)).thenReturn(Optional.of(existingProfile));
        when(profileRepository.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));

        when(jwtService.generateAccessToken(eq(userId), eq(email), anyString(), anyString()))
                .thenReturn("mock-access-token");

        GoogleAuthCommand command = new GoogleAuthCommand(token, null);
        AuthTokenResult result = useCase.execute(command);

        assertNotNull(result);
        assertEquals("mock-access-token", result.accessToken());

        ArgumentCaptor<ExternalLogin> captor = ArgumentCaptor.forClass(ExternalLogin.class);
        verify(externalLoginRepository).save(captor.capture());
        assertEquals("GOOGLE", captor.getValue().getProvider());
        assertEquals(sub, captor.getValue().getProviderUserId());
        assertEquals(userId, captor.getValue().getUserId());
    }

    @Test
    @DisplayName("Đăng ký mới tài khoản: mặc định role là STUDENT khi chưa có tài khoản")
    void testGoogleLogin_NewUser_StudentByDefault() {
        String token = "valid-google-id-token";
        String sub = "google-sub-fresh-111";
        String email = "fresh@gmail.com";

        GoogleUserClaims claims = new GoogleUserClaims(
                sub, email, true, "Fresh User", null,
                "accounts.google.com", "bee-client-id"
        );
        when(googleTokenVerifier.verify(token)).thenReturn(claims);
        when(externalLoginRepository.findByProviderAndProviderUserId("GOOGLE", sub)).thenReturn(Optional.empty());
        when(profileRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$RandomPasswordHash");
        when(profileRepository.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));

        when(jwtService.generateAccessToken(any(), eq(email), eq("student"), eq("Fresh User")))
                .thenReturn("mock-fresh-jwt");

        GoogleAuthCommand command = new GoogleAuthCommand(token, null);
        AuthTokenResult result = useCase.execute(command);

        assertNotNull(result);
        assertEquals("mock-fresh-jwt", result.accessToken());
        assertEquals("student", result.user().role());

        verify(profileRepository, atLeastOnce()).save(any(Profile.class));
        verify(externalLoginRepository).save(any(ExternalLogin.class));
    }

    @Test
    @DisplayName("Đăng ký mới tài khoản: cấp quyền PARENT khi accountType là parent")
    void testGoogleLogin_NewUser_ParentExplicit() {
        String token = "valid-google-id-token";
        String sub = "google-sub-parent-222";
        String email = "parent@gmail.com";

        GoogleUserClaims claims = new GoogleUserClaims(
                sub, email, true, "Parent User", null,
                "accounts.google.com", "bee-client-id"
        );
        when(googleTokenVerifier.verify(token)).thenReturn(claims);
        when(externalLoginRepository.findByProviderAndProviderUserId("GOOGLE", sub)).thenReturn(Optional.empty());
        when(profileRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$RandomPasswordHash");
        when(profileRepository.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));

        when(jwtService.generateAccessToken(any(), eq(email), eq("parent"), eq("Parent User")))
                .thenReturn("mock-parent-jwt");

        GoogleAuthCommand command = new GoogleAuthCommand(token, "parent");
        AuthTokenResult result = useCase.execute(command);

        assertNotNull(result);
        assertEquals("parent", result.user().role());
    }

    @Test
    @DisplayName("Zero-trust role: Yêu cầu role TEACHER hoặc ADMIN từ frontend sẽ bị từ chối và mặc định là STUDENT")
    void testGoogleLogin_NewUser_AttemptTeacherRole_DefaultsToStudent() {
        String token = "valid-google-id-token";
        String sub = "google-sub-attacker-333";
        String email = "attacker@gmail.com";

        GoogleUserClaims claims = new GoogleUserClaims(
                sub, email, true, "Hacker User", null,
                "accounts.google.com", "bee-client-id"
        );
        when(googleTokenVerifier.verify(token)).thenReturn(claims);
        when(externalLoginRepository.findByProviderAndProviderUserId("GOOGLE", sub)).thenReturn(Optional.empty());
        when(profileRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$RandomPasswordHash");
        when(profileRepository.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));

        when(jwtService.generateAccessToken(any(), eq(email), eq("student"), anyString()))
                .thenReturn("mock-jwt");

        // Client cố tình gửi role Teacher
        GoogleAuthCommand command = new GoogleAuthCommand(token, "teacher");
        AuthTokenResult result = useCase.execute(command);

        assertEquals("student", result.user().role());
    }

    @Test
    @DisplayName("Từ chối cấp token nếu tài khoản đã bị vô hiệu hóa (isBlocked)")
    void testGoogleLogin_BlockedAccount_ThrowsDomainException() {
        String token = "valid-google-id-token";
        String sub = "google-sub-blocked-444";
        String email = "blocked@beeacademy.edu.vn";
        UUID userId = UUID.randomUUID();

        GoogleUserClaims claims = new GoogleUserClaims(
                sub, email, true, "Blocked User", null,
                "accounts.google.com", "bee-client-id"
        );
        when(googleTokenVerifier.verify(token)).thenReturn(claims);

        ExternalLogin externalLogin = ExternalLogin.create(userId, "GOOGLE", sub, email);
        when(externalLoginRepository.findByProviderAndProviderUserId("GOOGLE", sub))
                .thenReturn(Optional.of(externalLogin));

        Profile blockedProfile = Profile.createNew(
                userId, email, PasswordHash.of("$2a$12$Hash"), UserRole.STUDENT, "Blocked User"
        );
        blockedProfile.block();
        when(profileRepository.findById(userId)).thenReturn(Optional.of(blockedProfile));

        GoogleAuthCommand command = new GoogleAuthCommand(token, null);
        DomainException ex = assertThrows(DomainException.class, () -> useCase.execute(command));
        assertEquals("ACCOUNT_BLOCKED", ex.getErrorCode());

        verify(jwtService, never()).generateAccessToken(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Từ chối nếu email của tài khoản Google chưa được xác minh (email_verified = false)")
    void testGoogleLogin_UnverifiedEmail_ThrowsDomainException() {
        String token = "unverified-google-id-token";
        GoogleUserClaims claims = new GoogleUserClaims(
                "sub-unverified", "unverified@gmail.com", false, "Unverified User", null,
                "accounts.google.com", "bee-client-id"
        );
        when(googleTokenVerifier.verify(token)).thenReturn(claims);

        GoogleAuthCommand command = new GoogleAuthCommand(token, null);
        DomainException ex = assertThrows(DomainException.class, () -> useCase.execute(command));
        assertEquals("GOOGLE_EMAIL_NOT_VERIFIED", ex.getErrorCode());
    }
}

