package com.beeacademy.backend.api.auth;

import com.beeacademy.backend.api.common.GlobalExceptionHandler;
import com.beeacademy.backend.application.auth.dto.AuthTokenResult;
import com.beeacademy.backend.application.auth.dto.UserProfileResult;
import com.beeacademy.backend.application.auth.usecase.*;
import com.beeacademy.backend.domain.auth.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử Tầng Controller: AuthController (HTTP, Cookie & JSON Envelopes)")
class AuthControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private RegisterUserUseCase registerUserUseCase;

    @Mock
    private AuthenticateUserUseCase authenticateUserUseCase;

    @Mock
    private AuthenticateWithGoogleUseCase authenticateWithGoogleUseCase;

    @Mock
    private RefreshTokenUseCase refreshTokenUseCase;

    @Mock
    private LogoutUseCase logoutUseCase;

    @Mock
    private ForgotPasswordUseCase forgotPasswordUseCase;

    @Mock
    private ResetPasswordUseCase resetPasswordUseCase;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/auth/login - Đăng nhập thành công trả về AccessToken trong Body và RefreshToken trong HttpOnly Cookie")
    void testLoginSuccessSetsHttpOnlyCookie() throws Exception {
        UUID userId = UUID.randomUUID();
        UserProfileResult user = new UserProfileResult(
                userId,
                "student@beeacademy.edu.vn",
                "student",
                "Nguyen Van A",
                null,
                null,
                null,
                "none",
                false,
                Instant.now()
        );

        AuthTokenResult tokenResult = new AuthTokenResult(
                "jwt.access.token.15m",
                "raw_refresh_token_7d",
                user
        );

        when(authenticateUserUseCase.execute(any())).thenReturn(tokenResult);

        String loginJson = """
                {
                    "email": "student@beeacademy.edu.vn",
                    "password": "Password@123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("jwt.access.token.15m"))
                .andExpect(jsonPath("$.data.user.email").value("student@beeacademy.edu.vn"))
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refreshToken=raw_refresh_token_7d")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("SameSite=Strict")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Path=/api/auth")));
    }

    @Test
    @DisplayName("POST /api/auth/refresh - Nhận diện Refresh Token từ HttpOnly Cookie và cấp phát phiên mới")
    void testRefreshSessionWithCookie() throws Exception {
        UUID userId = UUID.randomUUID();
        UserProfileResult user = new UserProfileResult(
                userId,
                "student@beeacademy.edu.vn",
                "student",
                "Nguyen Van A",
                null,
                null,
                null,
                "none",
                false,
                Instant.now()
        );

        AuthTokenResult tokenResult = new AuthTokenResult(
                "new.jwt.access.token.15m",
                "new_rotated_refresh_token",
                user
        );

        when(refreshTokenUseCase.execute("my_cookie_refresh_token")).thenReturn(tokenResult);

        Cookie refreshCookie = new Cookie("refreshToken", "my_cookie_refresh_token");

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(refreshCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("new.jwt.access.token.15m"))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refreshToken=new_rotated_refresh_token")));
    }

    @Test
    @DisplayName("POST /api/auth/logout - Thu hồi token và set Cookie MaxAge=0 để xóa khỏi trình duyệt")
    void testLogoutClearsCookie() throws Exception {
        Cookie refreshCookie = new Cookie("refreshToken", "logout_token");

        mockMvc.perform(post("/api/auth/logout")
                        .cookie(refreshCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")));

        verify(logoutUseCase).execute("logout_token");
    }

    @Test
    @DisplayName("POST /api/auth/register - Chấp nhận cả role chữ thường 'student' và chữ hoa 'STUDENT'")
    void testRegisterAcceptsCaseInsensitiveRole() throws Exception {
        UUID userId = UUID.randomUUID();
        UserProfileResult user = new UserProfileResult(
                userId, "lowercase@beeacademy.edu.vn", "student", "Hoc Sinh A",
                null, null, null, "none", false, Instant.now()
        );
        when(registerUserUseCase.execute(any())).thenReturn(user);

        // Gửi role chữ thường "student"
        String lowerCaseJson = """
                {
                    "email": "lowercase@beeacademy.edu.vn",
                    "password": "Password@123",
                    "fullName": "Hoc Sinh A",
                    "role": "student"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(lowerCaseJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("lowercase@beeacademy.edu.vn"));

        // Gửi role chữ hoa "STUDENT"
        String upperCaseJson = """
                {
                    "email": "lowercase@beeacademy.edu.vn",
                    "password": "Password@123",
                    "fullName": "Hoc Sinh A",
                    "role": "STUDENT"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(upperCaseJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /api/auth/register - Kiểm tra Validation: Bắt lỗi nếu email sai định dạng hoặc mật khẩu < 6 ký tự")
    void testRegisterValidationErrors() throws Exception {
        String invalidRequestJson = """
                {
                    "email": "not-an-email",
                    "password": "123",
                    "fullName": "",
                    "role": "STUDENT"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    @DisplayName("POST /api/auth/google - Đăng nhập Google thành công trả về AccessToken trong Body và RefreshToken trong HttpOnly Cookie")
    void testGoogleLoginSuccess() throws Exception {
        UUID userId = UUID.randomUUID();
        UserProfileResult user = new UserProfileResult(
                userId,
                "googleuser@gmail.com",
                "student",
                "Google Student",
                "https://lh3.googleusercontent.com/photo.jpg",
                null,
                null,
                "none",
                false,
                Instant.now()
        );

        AuthTokenResult tokenResult = new AuthTokenResult(
                "jwt.google.access.token",
                "raw_google_refresh_token",
                user
        );

        when(authenticateWithGoogleUseCase.execute(any())).thenReturn(tokenResult);

        String googleAuthJson = """
                {
                    "idToken": "valid-google-id-token-xyz",
                    "accountType": "student"
                }
                """;

        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(googleAuthJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("jwt.google.access.token"))
                .andExpect(jsonPath("$.data.user.email").value("googleuser@gmail.com"))
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refreshToken=raw_google_refresh_token")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("SameSite=Strict")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Path=/api/auth")));
    }

    @Test
    @DisplayName("POST /api/auth/google - Bắt lỗi validation nếu idToken để trống")
    void testGoogleLoginValidationBlankToken() throws Exception {
        String invalidJson = """
                {
                    "idToken": "",
                    "accountType": "student"
                }
                """;

        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }
}

