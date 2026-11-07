package com.beeacademy.backend.api.support;

import com.beeacademy.backend.api.common.GlobalExceptionHandler;
import com.beeacademy.backend.application.support.dto.NotificationResult;
import com.beeacademy.backend.application.support.usecase.ManageNotificationUseCase;
import com.beeacademy.backend.platform.security.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("NotificationController MockMvc Tests")
class NotificationControllerTest {

    private MockMvc mockMvc;
    private ManageNotificationUseCase manageNotificationUseCase;

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        manageNotificationUseCase = Mockito.mock(ManageNotificationUseCase.class);
        NotificationController controller = new NotificationController(manageNotificationUseCase);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        UserPrincipal principal = new UserPrincipal(userId, "user@bee.edu.vn", "ROLE_STUDENT");
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("GET /api/notifications - Lấy danh sách thông báo")
    void getMyNotifications_Success() throws Exception {
        NotificationResult result = new NotificationResult(
                UUID.randomUUID(), userId, "Khóa học mới", "Đã có khóa học Toán mới",
                "COURSE_UPDATE", "/courses/1", false, Instant.now(), null
        );

        when(manageNotificationUseCase.getUserNotifications(eq(userId))).thenReturn(List.of(result));

        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("Khóa học mới"))
                .andExpect(jsonPath("$.data[0].read").value(false));
    }

    @Test
    @DisplayName("POST /api/notifications/{id}/read - Đánh dấu thông báo đã đọc")
    void markAsRead_Success() throws Exception {
        UUID notifId = UUID.randomUUID();
        NotificationResult result = new NotificationResult(
                notifId, userId, "Khóa học mới", "Đã có khóa học Toán mới",
                "COURSE_UPDATE", "/courses/1", true, Instant.now(), Instant.now()
        );

        when(manageNotificationUseCase.markAsRead(eq(notifId), eq(userId))).thenReturn(result);

        mockMvc.perform(post("/api/notifications/" + notifId + "/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.read").value(true));
    }
}

