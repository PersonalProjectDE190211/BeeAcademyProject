package com.beeacademy.backend.api.auth;

import com.beeacademy.backend.api.auth.dto.*;
import com.beeacademy.backend.api.common.ApiResponse;
import com.beeacademy.backend.application.auth.dto.*;
import com.beeacademy.backend.application.auth.usecase.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

/**
 * Controller xử lý các luồng Xác thực Native Auth (UC01, UC02, UC03):
 * - Đăng ký, Đăng nhập, Làm mới Token, Đăng xuất, Quên & Đặt lại mật khẩu.
 * - Refresh Token được lưu trữ và bảo vệ nghiêm ngặt bằng HttpOnly Cookie (SameSite=Strict, Path=/api/auth).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private static final Duration REFRESH_COOKIE_MAX_AGE = Duration.ofDays(7);

    private final RegisterUserUseCase registerUserUseCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final ForgotPasswordUseCase forgotPasswordUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;

    @Value("${app.security.cookie.secure:false}")
    private boolean secureCookie;

    public AuthController(RegisterUserUseCase registerUserUseCase,
                          AuthenticateUserUseCase authenticateUserUseCase,
                          RefreshTokenUseCase refreshTokenUseCase,
                          LogoutUseCase logoutUseCase,
                          ForgotPasswordUseCase forgotPasswordUseCase,
                          ResetPasswordUseCase resetPasswordUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.authenticateUserUseCase = authenticateUserUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.logoutUseCase = logoutUseCase;
        this.forgotPasswordUseCase = forgotPasswordUseCase;
        this.resetPasswordUseCase = resetPasswordUseCase;
    }

    /**
     * UC01: Đăng ký tài khoản người dùng mới.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserProfileResult>> register(@Valid @RequestBody RegisterRequest request) {
        RegisterCommand command = new RegisterCommand(
                request.email(),
                request.password(),
                request.fullName(),
                request.role()
        );
        UserProfileResult profile = registerUserUseCase.execute(command);
        return ResponseEntity.ok(ApiResponse.success("Đăng ký tài khoản thành công", profile));
    }

    /**
     * UC02: Đăng nhập hệ thống - Nhận Access Token trong body và đính kèm Refresh Token trong HttpOnly Cookie.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseData>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest
    ) {
        String ipAddress = servletRequest.getRemoteAddr();
        String deviceInfo = servletRequest.getHeader(HttpHeaders.USER_AGENT);

        LoginCommand command = new LoginCommand(
                request.email(),
                request.password(),
                deviceInfo,
                ipAddress
        );

        AuthTokenResult tokenResult = authenticateUserUseCase.execute(command);

        ResponseCookie cookie = buildRefreshTokenCookie(tokenResult.refreshToken(), REFRESH_COOKIE_MAX_AGE);
        AuthResponseData responseData = new AuthResponseData(tokenResult.accessToken(), tokenResult.user());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.success("Đăng nhập thành công", responseData));
    }

    /**
     * UC02: Làm mới phiên đăng nhập (Refresh Token Rotation).
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponseData>> refresh(
            @CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) String cookieRefreshToken,
            @RequestBody(required = false) RefreshTokenFallbackRequest bodyRequest
    ) {
        String rawToken = cookieRefreshToken;
        if ((rawToken == null || rawToken.isBlank()) && bodyRequest != null) {
            rawToken = bodyRequest.refreshToken();
        }

        AuthTokenResult tokenResult = refreshTokenUseCase.execute(rawToken);

        ResponseCookie cookie = buildRefreshTokenCookie(tokenResult.refreshToken(), REFRESH_COOKIE_MAX_AGE);
        AuthResponseData responseData = new AuthResponseData(tokenResult.accessToken(), tokenResult.user());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.success("Làm mới phiên làm việc thành công", responseData));
    }

    /**
     * UC02: Đăng xuất người dùng - Thu hồi token và xóa sạch HttpOnly Cookie.
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) String cookieRefreshToken,
            @RequestBody(required = false) RefreshTokenFallbackRequest bodyRequest
    ) {
        String rawToken = cookieRefreshToken;
        if ((rawToken == null || rawToken.isBlank()) && bodyRequest != null) {
            rawToken = bodyRequest.refreshToken();
        }

        logoutUseCase.execute(rawToken);
        ResponseCookie clearCookie = buildRefreshTokenCookie("", Duration.ZERO);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
                .body(ApiResponse.success("Đăng xuất thành công", null));
    }

    /**
     * UC03: Quên mật khẩu - gửi mã OTP xác nhận về email.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        forgotPasswordUseCase.execute(new ForgotPasswordCommand(request.email()));
        return ResponseEntity.ok(ApiResponse.success("Mã xác thực đã được gửi về email của bạn", null));
    }

    /**
     * UC03: Đặt lại mật khẩu mới qua mã OTP.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        resetPasswordUseCase.execute(new ResetPasswordCommand(
                request.email(),
                request.otp(),
                request.newPassword()
        ));
        return ResponseEntity.ok(ApiResponse.success("Đặt lại mật khẩu thành công. Vui lòng đăng nhập lại.", null));
    }

    private ResponseCookie buildRefreshTokenCookie(String value, Duration maxAge) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, value)
                .httpOnly(true)
                .secure(secureCookie)
                .path("/api/auth")
                .maxAge(maxAge)
                .sameSite("Strict")
                .build();
    }

    public record RefreshTokenFallbackRequest(String refreshToken) {}
}

