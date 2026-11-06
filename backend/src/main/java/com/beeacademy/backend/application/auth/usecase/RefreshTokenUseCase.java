package com.beeacademy.backend.application.auth.usecase;

import com.beeacademy.backend.application.auth.dto.AuthTokenResult;
import com.beeacademy.backend.application.auth.dto.UserProfileResult;
import com.beeacademy.backend.domain.auth.Profile;
import com.beeacademy.backend.domain.auth.ProfileRepositoryPort;
import com.beeacademy.backend.domain.auth.RefreshToken;
import com.beeacademy.backend.domain.auth.RefreshTokenRepositoryPort;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.platform.security.JwtService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Use case thực hiện Xoay vòng Refresh Token (Refresh Token Rotation - RFC 6749)
 * kèm cơ chế Phát hiện chiếm đoạt phiên (Token Reuse/Theft Detection)
 * và kiểm tra trạng thái tài khoản thời gian thực (Zero-lag Account Status Check).
 */
@Service
public class RefreshTokenUseCase {

    private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(7);

    private final ProfileRepositoryPort profileRepository;
    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final JwtService jwtService;

    public RefreshTokenUseCase(ProfileRepositoryPort profileRepository,
                              RefreshTokenRepositoryPort refreshTokenRepository,
                              JwtService jwtService) {
        this.profileRepository = profileRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthTokenResult execute(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new DomainException("INVALID_REFRESH_TOKEN", "Refresh token không được để trống");
        }

        String tokenHash = AuthenticateUserUseCase.hashSha256(rawRefreshToken.trim());
        RefreshToken existingToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new DomainException("INVALID_REFRESH_TOKEN", "Refresh token không hợp lệ hoặc không tồn tại"));

        Instant now = Instant.now();

        // 1. PHÁT HIỆN HÀNH VI TÁI SỬ DỤNG TOKEN ĐÃ BỊ THU HỒI (TOKEN REUSE / THEFT DETECTION)
        if (existingToken.isRevoked()) {
            // Nghiêm trọng: Token cũ đã bị xoay vòng hoặc logout mà vẫn được gửi lên -> có dấu hiệu bị đánh cắp!
            // Lập tức thu hồi toàn bộ phiên của người dùng này để bảo vệ tài khoản.
            refreshTokenRepository.revokeAllByUserId(existingToken.getUserId(), now);
            throw new DomainException("TOKEN_THEFT_DETECTED",
                    "Cảnh báo bảo mật: Phát hiện phiên đăng nhập bất thường. Toàn bộ phiên làm việc của bạn đã được đăng xuất.");
        }

        // 2. KIỂM TRA HẾT HẠN
        if (existingToken.isExpired(now)) {
            throw new DomainException("TOKEN_EXPIRED", "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
        }

        // 3. ZERO-LAG ACCOUNT STATUS CHECK: Kiểm tra trạng thái tài khoản trong Database
        Profile profile = profileRepository.findById(existingToken.getUserId())
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "Không tìm thấy hồ sơ người dùng"));

        if (profile.isBlocked()) {
            existingToken.revoke(now);
            refreshTokenRepository.save(existingToken);
            throw new DomainException("ACCOUNT_BLOCKED", "Tài khoản của bạn đã bị khóa hoặc vô hiệu hóa bởi Quản trị viên");
        }

        if (profile.isTemporarilyLocked(now)) {
            existingToken.revoke(now);
            refreshTokenRepository.save(existingToken);
            throw new DomainException("ACCOUNT_TEMPORARILY_LOCKED", "Tài khoản đang bị tạm khóa. Vui lòng thử lại sau.");
        }

        // 4. XOAY VÒNG TOKEN (ROTATION)
        String newRawRefreshToken = UUID.randomUUID().toString().replace("-", "") +
                                    UUID.randomUUID().toString().replace("-", "");
        String newTokenHash = AuthenticateUserUseCase.hashSha256(newRawRefreshToken);

        existingToken.rotate(newTokenHash, now);
        refreshTokenRepository.save(existingToken);

        RefreshToken newToken = RefreshToken.issue(
                profile.getId(),
                newTokenHash,
                REFRESH_TOKEN_TTL,
                existingToken.getFamilyId()
        );
        refreshTokenRepository.save(newToken);

        // 5. CẤP PHÁT ACCESS TOKEN MỚI (15 phút)
        String newAccessToken = jwtService.generateAccessToken(
                profile.getId(),
                profile.getEmail(),
                profile.getRole().getDbValue(),
                profile.getFullName()
        );

        return new AuthTokenResult(newAccessToken, newRawRefreshToken, UserProfileResult.from(profile));
    }
}
