package com.beeacademy.backend.application.auth.usecase;

import com.beeacademy.backend.application.auth.dto.AuthTokenResult;
import com.beeacademy.backend.application.auth.dto.LoginCommand;
import com.beeacademy.backend.application.auth.dto.UserProfileResult;
import com.beeacademy.backend.domain.auth.Profile;
import com.beeacademy.backend.domain.auth.ProfileRepositoryPort;
import com.beeacademy.backend.domain.auth.RefreshToken;
import com.beeacademy.backend.domain.auth.RefreshTokenRepositoryPort;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.platform.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Use case thực hiện Xác thực đăng nhập người dùng (UC02).
 * Quản lý cơ chế chống brute-force (khóa 15 phút sau 5 lần sai - REQ-AUTH-002)
 * và cấp phát JWT Access Token + Refresh Token (lưu bản băm SHA-256).
 */
@Service
public class AuthenticateUserUseCase {

    private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(7);

    private final ProfileRepositoryPort profileRepository;
    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthenticateUserUseCase(ProfileRepositoryPort profileRepository,
                                  RefreshTokenRepositoryPort refreshTokenRepository,
                                  PasswordEncoder passwordEncoder,
                                  JwtService jwtService) {
        this.profileRepository = profileRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthTokenResult execute(LoginCommand command) {
        if (command == null) {
            throw new DomainException("INVALID_COMMAND", "Thông tin đăng nhập không được rỗng");
        }

        String email = command.email().trim().toLowerCase();
        Profile profile = profileRepository.findByEmail(email)
                .orElseThrow(() -> new DomainException("INVALID_CREDENTIALS", "Email hoặc mật khẩu không chính xác"));

        Instant now = Instant.now();

        if (profile.isBlocked()) {
            throw new DomainException("ACCOUNT_BLOCKED", "Tài khoản của bạn đã bị vô hiệu hóa bởi Quản trị viên");
        }

        if (profile.isTemporarilyLocked(now)) {
            long remainingSec = profile.remainingLockSeconds(now);
            throw new DomainException("ACCOUNT_TEMPORARILY_LOCKED",
                    String.format("Tài khoản đang bị tạm khóa do nhập sai nhiều lần. Vui lòng thử lại sau %d phút %d giây.",
                            remainingSec / 60, remainingSec % 60));
        }

        boolean passwordMatches = passwordEncoder.matches(
                command.password(),
                profile.getPasswordHash().value()
        );

        if (!passwordMatches) {
            profile.registerFailedLogin(now);
            profileRepository.save(profile);

            if (profile.isTemporarilyLocked(now)) {
                throw new DomainException("ACCOUNT_TEMPORARILY_LOCKED",
                        "Bạn đã nhập sai mật khẩu 5 lần liên tiếp. Tài khoản bị tạm khóa 15 phút để bảo mật.");
            }
            throw new DomainException("INVALID_CREDENTIALS", "Email hoặc mật khẩu không chính xác");
        }

        // Đăng nhập thành công -> reset lockout counter
        profile.resetFailedLogin();
        profileRepository.save(profile);

        // Sinh JWT Access Token (15 phút)
        String accessToken = jwtService.generateAccessToken(
                profile.getId(),
                profile.getEmail(),
                profile.getRole().getDbValue(),
                profile.getFullName()
        );

        // Sinh raw Refresh Token ngẫu nhiên (64 hex characters)
        String rawRefreshToken = generateRawToken();
        String tokenHash = hashSha256(rawRefreshToken);

        // Lưu Refresh Token (chỉ lưu bản băm SHA-256)
        RefreshToken refreshToken = RefreshToken.issue(profile.getId(), tokenHash, REFRESH_TOKEN_TTL);
        refreshTokenRepository.save(refreshToken);

        return new AuthTokenResult(accessToken, rawRefreshToken, UserProfileResult.from(profile));
    }

    public static String hashSha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Không tìm thấy thuật toán SHA-256", e);
        }
    }

    private String generateRawToken() {
        return UUID.randomUUID().toString().replace("-", "") +
               UUID.randomUUID().toString().replace("-", "");
    }
}
