package com.beeacademy.backend.api.family;

import com.beeacademy.backend.api.common.GlobalExceptionHandler;
import com.beeacademy.backend.api.family.dto.InviteStudentRequest;
import com.beeacademy.backend.application.family.dto.ParentLinkResult;
import com.beeacademy.backend.application.family.dto.StudentCourseProgressItem;
import com.beeacademy.backend.application.family.dto.StudentProgressReportResult;
import com.beeacademy.backend.application.family.usecase.ManageParentLinkUseCase;
import com.beeacademy.backend.application.family.usecase.ParentViewStudentProgressUseCase;
import com.beeacademy.backend.platform.security.UserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("ParentPortalController MockMvc Tests")
class ParentPortalControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private ManageParentLinkUseCase manageParentLinkUseCase;
    private ParentViewStudentProgressUseCase parentViewStudentProgressUseCase;

    private final UUID parentId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        manageParentLinkUseCase = Mockito.mock(ManageParentLinkUseCase.class);
        parentViewStudentProgressUseCase = Mockito.mock(ParentViewStudentProgressUseCase.class);

        ParentPortalController controller = new ParentPortalController(
                manageParentLinkUseCase, parentViewStudentProgressUseCase
        );

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        UserPrincipal principal = new UserPrincipal(parentId, "parent@bee.edu.vn", "ROLE_PARENT");
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority("ROLE_PARENT"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("POST /api/parent/links/invite - Gửi lời mời liên kết thành công")
    void inviteStudent_Success() throws Exception {
        InviteStudentRequest request = new InviteStudentRequest("child@bee.edu.vn");
        ParentLinkResult result = new ParentLinkResult(
                UUID.randomUUID(), parentId, null, "child@bee.edu.vn", "PAR123", "PENDING",
                false, Instant.now().plusSeconds(3600), null, null, null
        );

        when(manageParentLinkUseCase.inviteStudent(any())).thenReturn(result);

        mockMvc.perform(post("/api/parent/links/invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.studentEmail").value("child@bee.edu.vn"))
                .andExpect(jsonPath("$.data.inviteToken").value("PAR123"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @DisplayName("GET /api/parent/students/{studentId}/progress - Xem tiến độ học tập của con")
    void getStudentProgress_Success() throws Exception {
        UUID studentId = UUID.randomUUID();
        StudentCourseProgressItem courseItem = new StudentCourseProgressItem(
                UUID.randomUUID(), "Luyện thi Toán 10", 85.5, false, Instant.now()
        );
        StudentProgressReportResult report = new StudentProgressReportResult(
                studentId, "child@bee.edu.vn", 1, 0, List.of(courseItem)
        );

        when(parentViewStudentProgressUseCase.execute(eq(parentId), eq(studentId))).thenReturn(report);

        mockMvc.perform(get("/api/parent/students/" + studentId + "/progress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.enrolledCoursesCount").value(1))
                .andExpect(jsonPath("$.data.courses[0].courseTitle").value("Luyện thi Toán 10"))
                .andExpect(jsonPath("$.data.courses[0].progressPercent").value(85.5));
    }
}

