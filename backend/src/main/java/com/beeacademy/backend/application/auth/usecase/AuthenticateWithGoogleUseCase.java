package com.beeacademy.backend.application.auth.usecase;

import com.beeacademy.backend.application.auth.dto.AuthTokenResult;
import com.beeacademy.backend.application.auth.dto.GoogleAuthCommand;
import com.beeacademy.backend.application.auth.dto.UserProfileResult;
import com.beeacademy.backend.domain.auth.*;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.platform.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Use case: Xác thực & Đăng nhập bằng Google OAuth2 ID Token (UC02-Google).
 *
 * Đảm bảo 5 nguyên tắc bảo mật:
 * 1. Zero-trust role từ client: Chỉ cho phép tự khởi tạo STUDENT hoặc PARENT, cấm tự nâng quyền TEACHER/ADMIN.
 * 2. Định danh Google bằng `sub` qua bảng ExternalLogin (1-1 hoặc 1-N liên kết tài khoản).
 * 3. Kiểm tra tính hợp lệ và claim bắt buộc của Google Token (email_verified, aud, iss, exp).
 * 4. Kiểm tra trạng thái tài khoản (bị khóa vĩnh viễn/tạm thời) trước khi phát hành token.
 * 5. Cấp phát Access Token + Refresh Token (chỉ lưu hash SHA-256 trong CSDL).
 */
@Service
public class AuthenticateWithGoogleUseCase {

    private static final String PROVIDER_GOOGLE = "GOOGLE";
    private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(7);

    private final GoogleTokenVerifierPort googleTokenVerifier;
    private final ExternalLoginRepositoryPort externalLoginRepository;
    private final ProfileRepositoryPort profileRepository;
    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthenticateWithGoogleUseCase(GoogleTokenVerifierPort googleTokenVerifier,
                                         ExternalLoginRepositoryPort externalLoginRepository,
                                         ProfileRepositoryPort profileRepository,
                                         RefreshTokenRepositoryPort refreshTokenRepository,
                                         PasswordEncoder passwordEncoder,
                                         JwtService jwtService) {
        this.googleTokenVerifier = googleTokenVerifier;
        this.externalLoginRepository = externalLoginRepository;
        this.profileRepository = profileRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthTokenResult execute(GoogleAuthCommand command) {
        if (command == null || command.idToken() == null || command.idToken().isBlank()) {
            throw new DomainException("INVALID_TOKEN", "Google ID Token không được để trống");
        }

        // 1. Xác thực Google ID Token và trích xuất claims chuẩn
        GoogleUserClaims claims = googleTokenVerifier.verify(command.idToken());

        if (!claims.emailVerified()) {
            throw new DomainException("GOOGLE_EMAIL_NOT_VERIFIED",
                    "Email Google chưa được xác minh bởi Google, không thể sử dụng để đăng nhập");
        }

        String googleSub = claims.sub();
        String normalizedEmail = claims.email().trim().toLowerCase();

        // 2. Tìm tài khoản qua liên kết ExternalLogin (định danh bằng Provider + sub)
        Optional<ExternalLogin> existingLogin = externalLoginRepository.findByProviderAndProviderUserId(
                PROVIDER_GOOGLE,
                googleSub
        );

        Profile profile;

        if (existingLogin.isPresent()) {
            // Đã liên kết trước đó -> lấy hồ sơ người dùng
            UUID userId = existingLogin.get().getUserId();
            profile = profileRepository.findById(userId)
                    .orElseThrow(() -> new DomainException("USER_NOT_FOUND",
                            "Không tìm thấy hồ sơ người dùng tương ứng với tài khoản Google đã liên kết"));
        } else {
            // Chưa có bản ghi ExternalLogin với sub này -> kiểm tra theo email
            Optional<Profile> profileByEmail = profileRepository.findByEmail(normalizedEmail);

            if (profileByEmail.isPresent()) {
                // Email đã tồn tại & Google email đã được xác minh -> tự động liên kết tài khoản an toàn
                profile = profileByEmail.get();
                ExternalLogin link = ExternalLogin.create(profile.getId(), PROVIDER_GOOGLE, googleSub, normalizedEmail);
                externalLoginRepository.save(link);

                // Cập nhật avatar nếu hồ sơ cũ chưa có
                if ((profile.getAvatarUrl() == null || profile.getAvatarUrl().isBlank())
                        && claims.avatarUrl() != null && !claims.avatarUrl().isBlank()) {
                    profile.changeAvatar(claims.avatarUrl().trim());
                    profile = profileRepository.save(profile);
                }
            } else {
                // Người dùng hoàn toàn mới -> Khởi tạo tài khoản
                // Zero-trust role: Chỉ cho phép "parent" nếu chỉ định rõ, còn lại mặc định là STUDENT
                UserRole initialRole = "parent".equalsIgnoreCase(command.accountType())
                        ? UserRole.PARENT
                        : UserRole.STUDENT;

                UUID newUserId = UUID.randomUUID();
                String randomPassword = UUID.randomUUID().toString() + UUID.randomUUID().toString();
                String encodedPassword = passwordEncoder.encode(randomPassword);

                String displayName = claims.fullName() != null && !claims.fullName().isBlank()
                        ? claims.fullName().trim()
                        : normalizedEmail.split("@")[0];

                Profile newProfile = Profile.createNew(
                        newUserId,
                        normalizedEmail,
                        PasswordHash.of(encodedPassword),
                        initialRole,
                        displayName
                );

                if (claims.avatarUrl() != null && !claims.avatarUrl().isBlank()) {
                    newProfile.changeAvatar(claims.avatarUrl().trim());
                }

                profile = profileRepository.save(newProfile);

                ExternalLogin newLink = ExternalLogin.create(newUserId, PROVIDER_GOOGLE, googleSub, normalizedEmail);
                externalLoginRepository.save(newLink);
            }
        }

        // 3. Kiểm tra trạng thái tài khoản trước khi cấp phát JWT
        Instant now = Instant.now();

        if (profile.isBlocked()) {
            throw new DomainException("ACCOUNT_BLOCKED", "Tài khoản của bạn đã bị vô hiệu hóa bởi Quản trị viên");
        }

        if (profile.isTemporarilyLocked(now)) {
            long remainingSec = profile.remainingLockSeconds(now);
            throw new DomainException("ACCOUNT_TEMPORARILY_LOCKED",
                    String.format("Tài khoản đang bị tạm khóa. Vui lòng thử lại sau %d phút %d giây.",
                            remainingSec / 60, remainingSec % 60));
        }

        // Đăng nhập thành công -> Xóa bộ đếm đăng nhập sai nếu có
        profile.resetFailedLogin();
        profileRepository.save(profile);

        // 4. Phát hành JWT Access Token (15 phút)
        String accessToken = jwtService.generateAccessToken(
                profile.getId(),
                profile.getEmail(),
                profile.getRole().getDbValue(),
                profile.getFullName()
        );

        // 5. Phát hành Refresh Token (7 ngày, lưu SHA-256 hash vào DB)
        String rawRefreshToken = generateRawToken();
        String tokenHash = AuthenticateUserUseCase.hashSha256(rawRefreshToken);
        RefreshToken refreshToken = RefreshToken.issue(profile.getId(), tokenHash, REFRESH_TOKEN_TTL);
        refreshTokenRepository.save(refreshToken);

        return new AuthTokenResult(accessToken, rawRefreshToken, UserProfileResult.from(profile));
    }

    private String generateRawToken() {
        return UUID.randomUUID().toString().replace("-", "") +
               UUID.randomUUID().toString().replace("-", "");
    }
}

