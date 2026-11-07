package com.beeacademy.backend.api.family;

import com.beeacademy.backend.api.common.GlobalExceptionHandler;
import com.beeacademy.backend.api.family.dto.RespondLinkRequest;
import com.beeacademy.backend.application.family.dto.ParentLinkResult;
import com.beeacademy.backend.application.family.usecase.ManageParentLinkUseCase;
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

@DisplayName("StudentFamilyController MockMvc Tests")
class StudentFamilyControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private ManageParentLinkUseCase manageParentLinkUseCase;

    private final UUID studentId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        manageParentLinkUseCase = Mockito.mock(ManageParentLinkUseCase.class);

        StudentFamilyController controller = new StudentFamilyController(manageParentLinkUseCase);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        UserPrincipal principal = new UserPrincipal(studentId, "student@bee.edu.vn", "ROLE_STUDENT");
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
    @DisplayName("POST /api/student/family/links/accept - Chấp nhận lời mời liên kết thành công")
    void acceptInvite_Success() throws Exception {
        RespondLinkRequest request = new RespondLinkRequest("PAR123", null);
        ParentLinkResult result = new ParentLinkResult(
                UUID.randomUUID(), UUID.randomUUID(), studentId, "student@bee.edu.vn", "PAR123", "ACTIVE",
                true, Instant.now().plusSeconds(3600), Instant.now(), null, null
        );

        when(manageParentLinkUseCase.acceptInvite(any())).thenReturn(result);

        mockMvc.perform(post("/api/student/family/links/accept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.consentGranted").value(true));
    }

    @Test
    @DisplayName("GET /api/student/family/links - Lấy danh sách phụ huynh liên kết")
    void getMyParentLinks_Success() throws Exception {
        ParentLinkResult result = new ParentLinkResult(
                UUID.randomUUID(), UUID.randomUUID(), studentId, "student@bee.edu.vn", "PAR123", "ACTIVE",
                true, Instant.now().plusSeconds(3600), Instant.now(), null, null
        );

        when(manageParentLinkUseCase.getStudentLinks(eq(studentId))).thenReturn(List.of(result));

        mockMvc.perform(get("/api/student/family/links"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].status").value("ACTIVE"));
    }
}

