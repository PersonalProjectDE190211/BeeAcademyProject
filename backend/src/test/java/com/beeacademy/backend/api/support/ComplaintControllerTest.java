package com.beeacademy.backend.api.support;

import com.beeacademy.backend.api.common.GlobalExceptionHandler;
import com.beeacademy.backend.api.support.dto.AddComplaintMessageRequest;
import com.beeacademy.backend.api.support.dto.CreateComplaintRequest;
import com.beeacademy.backend.application.support.dto.ComplaintMessageResult;
import com.beeacademy.backend.application.support.dto.ComplaintResult;
import com.beeacademy.backend.application.support.usecase.ManageComplaintUseCase;
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

@DisplayName("ComplaintController MockMvc Tests")
class ComplaintControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private ManageComplaintUseCase manageComplaintUseCase;

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        manageComplaintUseCase = Mockito.mock(ManageComplaintUseCase.class);
        ComplaintController controller = new ComplaintController(manageComplaintUseCase);

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
    @DisplayName("POST /api/complaints - Tạo phiếu khiếu nại thành công")
    void createComplaint_Success() throws Exception {
        CreateComplaintRequest request = new CreateComplaintRequest(
                "Lỗi video không tải", "TECHNICAL", "HIGH", "Video bài 3 bị màn hình đen"
        );

        ComplaintResult result = new ComplaintResult(
                UUID.randomUUID(), userId, "STUDENT", "Lỗi video không tải", "TECHNICAL",
                "HIGH", "OPEN", Instant.now(), Instant.now(), null, null, null, List.of()
        );

        when(manageComplaintUseCase.createComplaint(any())).thenReturn(result);

        mockMvc.perform(post("/api/complaints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Lỗi video không tải"))
                .andExpect(jsonPath("$.data.status").value("OPEN"));
    }

    @Test
    @DisplayName("GET /api/complaints/my - Xem danh sách khiếu nại của tôi")
    void getMyComplaints_Success() throws Exception {
        ComplaintResult result = new ComplaintResult(
                UUID.randomUUID(), userId, "STUDENT", "Lỗi video", "TECHNICAL",
                "HIGH", "OPEN", Instant.now(), Instant.now(), null, null, null, List.of()
        );

        when(manageComplaintUseCase.getMyComplaints(eq(userId))).thenReturn(List.of(result));

        mockMvc.perform(get("/api/complaints/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("Lỗi video"));
    }
}

