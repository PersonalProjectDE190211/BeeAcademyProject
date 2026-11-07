package com.beeacademy.backend.api.admin;

import com.beeacademy.backend.api.admin.dto.LockUserRequest;
import com.beeacademy.backend.api.admin.dto.ProvisionTeacherRequest;
import com.beeacademy.backend.api.admin.dto.RejectTeacherRequest;
import com.beeacademy.backend.api.admin.dto.UnlockUserRequest;
import com.beeacademy.backend.api.common.GlobalExceptionHandler;
import com.beeacademy.backend.application.admin.dto.AdminUserDetailDto;
import com.beeacademy.backend.application.admin.dto.AdminUserSummaryDto;
import com.beeacademy.backend.application.admin.usecase.ManageUsersUseCase;
import com.beeacademy.backend.application.admin.usecase.ProvisionTeacherAccountUseCase;
import com.beeacademy.backend.platform.security.UserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("AdminUserController MockMvc Tests")
class AdminUserControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private ManageUsersUseCase manageUsersUseCase;
    private ProvisionTeacherAccountUseCase provisionTeacherAccountUseCase;

    private final UUID adminId = UUID.randomUUID();
    private final UUID targetUserId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        manageUsersUseCase = Mockito.mock(ManageUsersUseCase.class);
        provisionTeacherAccountUseCase = Mockito.mock(ProvisionTeacherAccountUseCase.class);

        AdminUserController controller = new AdminUserController(manageUsersUseCase, provisionTeacherAccountUseCase);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        UserPrincipal principal = new UserPrincipal(adminId, "admin@bee.edu.vn", "ROLE_ADMIN");
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("GET /api/admin/users trả về 200 và danh sách phân trang")
    void shouldSearchUsers() throws Exception {
        AdminUserSummaryDto user = new AdminUserSummaryDto(
                targetUserId,
                "student@bee.edu.vn",
                "Nguyễn Văn Học",
                "STUDENT",
                "ACTIVE",
                "NONE",
                true,
                Instant.now()
        );
        when(manageUsersUseCase.searchUsers(any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(user), org.springframework.data.domain.PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/admin/users")
                        .param("keyword", "Nguyễn")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].email").value("student@bee.edu.vn"));
    }

    @Test
    @DisplayName("POST /api/admin/users/{id}/lock khóa tài khoản thành công")
    void shouldLockUser() throws Exception {
        LockUserRequest req = new LockUserRequest("Vi phạm tiêu chuẩn cộng đồng");

        mockMvc.perform(post("/api/admin/users/" + targetUserId + "/lock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Khóa tài khoản thành công"));

        verify(manageUsersUseCase).lockUser(eq(targetUserId), eq("Vi phạm tiêu chuẩn cộng đồng"), eq(adminId));
    }

    @Test
    @DisplayName("POST /api/admin/users/{id}/unlock mở khóa tài khoản thành công")
    void shouldUnlockUser() throws Exception {
        UnlockUserRequest req = new UnlockUserRequest("Đã mở khóa sau giải trình");

        mockMvc.perform(post("/api/admin/users/" + targetUserId + "/unlock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Mở khóa tài khoản thành công"));

        verify(manageUsersUseCase).unlockUser(eq(targetUserId), eq("Đã mở khóa sau giải trình"), eq(adminId));
    }

    @Test
    @DisplayName("POST /api/admin/users/{id}/approve-teacher phê duyệt giáo viên thành công")
    void shouldApproveTeacher() throws Exception {
        mockMvc.perform(post("/api/admin/users/" + targetUserId + "/approve-teacher")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(provisionTeacherAccountUseCase).approveTeacher(eq(targetUserId), eq(adminId));
    }

    @Test
    @DisplayName("POST /api/admin/users/{id}/reject-teacher từ chối giáo viên thành công")
    void shouldRejectTeacher() throws Exception {
        RejectTeacherRequest req = new RejectTeacherRequest("Hồ sơ không hợp lệ");

        mockMvc.perform(post("/api/admin/users/" + targetUserId + "/reject-teacher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(provisionTeacherAccountUseCase).rejectTeacher(eq(targetUserId), eq("Hồ sơ không hợp lệ"), eq(adminId));
    }

    @Test
    @DisplayName("POST /api/admin/users/provision-teacher cấp tài khoản giáo viên thành công")
    void shouldProvisionTeacher() throws Exception {
        ProvisionTeacherRequest req = new ProvisionTeacherRequest(
                "teacher@bee.edu.vn",
                "Secret@123",
                "Thầy Toán Mới",
                "Giáo viên đợt 2"
        );

        AdminUserDetailDto teacherDto = new AdminUserDetailDto(
                targetUserId,
                "teacher@bee.edu.vn",
                "Thầy Toán Mới",
                null,
                null,
                null,
                null,
                "TEACHER",
                "ACTIVE",
                "APPROVED",
                true,
                0,
                null,
                true,
                adminId,
                "Giáo viên đợt 2",
                Instant.now(),
                Instant.now()
        );
        when(provisionTeacherAccountUseCase.provisionTeacherAccount(any(), eq(adminId))).thenReturn(teacherDto);

        mockMvc.perform(post("/api/admin/users/provision-teacher")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("teacher@bee.edu.vn"))
                .andExpect(jsonPath("$.data.teacherApprovalStatus").value("APPROVED"));
    }
}

