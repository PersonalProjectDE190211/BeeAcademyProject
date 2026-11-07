package com.beeacademy.backend.api.support;

import com.beeacademy.backend.api.common.GlobalExceptionHandler;
import com.beeacademy.backend.api.support.dto.ResolveComplaintRequest;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("AdminComplaintController MockMvc Tests")
class AdminComplaintControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private ManageComplaintUseCase manageComplaintUseCase;

    private final UUID adminId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        manageComplaintUseCase = Mockito.mock(ManageComplaintUseCase.class);
        AdminComplaintController controller = new AdminComplaintController(manageComplaintUseCase);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
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
    @DisplayName("GET /api/admin/complaints - Lấy danh sách toàn bộ khiếu nại")
    void getAllComplaints_Success() throws Exception {
        ComplaintResult result = new ComplaintResult(
                UUID.randomUUID(), UUID.randomUUID(), "STUDENT", "Lỗi nạp tiền", "PAYMENT",
                "URGENT", "OPEN", Instant.now(), Instant.now(), null, null, null, List.of()
        );

        when(manageComplaintUseCase.getAllComplaints(any())).thenReturn(List.of(result));

        mockMvc.perform(get("/api/admin/complaints"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("Lỗi nạp tiền"));
    }

    @Test
    @DisplayName("POST /api/admin/complaints/{id}/resolve - Admin giải quyết khiếu nại")
    void resolveComplaint_Success() throws Exception {
        UUID complaintId = UUID.randomUUID();
        ResolveComplaintRequest request = new ResolveComplaintRequest("Đã hoàn tiền vào tài khoản người dùng");

        ComplaintResult result = new ComplaintResult(
                complaintId, UUID.randomUUID(), "STUDENT", "Lỗi nạp tiền", "PAYMENT",
                "URGENT", "RESOLVED", Instant.now(), Instant.now(), Instant.now(), adminId,
                "Đã hoàn tiền vào tài khoản người dùng", List.of()
        );

        when(manageComplaintUseCase.resolveComplaint(any())).thenReturn(result);

        mockMvc.perform(post("/api/admin/complaints/" + complaintId + "/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("RESOLVED"))
                .andExpect(jsonPath("$.data.resolutionSummary").value("Đã hoàn tiền vào tài khoản người dùng"));
    }
}

