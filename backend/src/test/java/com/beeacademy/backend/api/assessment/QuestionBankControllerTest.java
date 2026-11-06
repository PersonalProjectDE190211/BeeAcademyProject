package com.beeacademy.backend.api.assessment;

import com.beeacademy.backend.api.common.GlobalExceptionHandler;
import com.beeacademy.backend.application.assessment.dto.ChoiceResult;
import com.beeacademy.backend.application.assessment.dto.QuestionResult;
import com.beeacademy.backend.application.assessment.usecase.ManageQuestionBankUseCase;
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
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("QuestionBankController MockMvc Tests")
class QuestionBankControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private ManageQuestionBankUseCase questionBankUseCase;

    private final UUID teacherId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        questionBankUseCase = Mockito.mock(ManageQuestionBankUseCase.class);

        QuestionBankController controller = new QuestionBankController(questionBankUseCase);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        UserPrincipal principal = new UserPrincipal(teacherId, "teacher@test.com", "TEACHER");
        var auth = new UsernamePasswordAuthenticationToken(principal, null, List.of(new SimpleGrantedAuthority("ROLE_TEACHER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("GET /api/teacher/questions -> Lấy danh sách câu hỏi của giáo viên")
    void getMyQuestions_Success() throws Exception {
        QuestionResult q = new QuestionResult(
                UUID.randomUUID(), teacherId, UUID.randomUUID(), UUID.randomUUID(),
                8, "Câu hỏi toán?", "Giải thích", "EASY", "MULTIPLE_CHOICE", "ACTIVE",
                0, Instant.now(), Instant.now(), List.of()
        );

        when(questionBankUseCase.getQuestionsByTeacher(teacherId)).thenReturn(List.of(q));

        mockMvc.perform(get("/api/teacher/questions")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].content").value("Câu hỏi toán?"));
    }

    @Test
    @DisplayName("POST /api/teacher/questions -> Tạo câu hỏi mới thành công")
    void createQuestion_Success() throws Exception {
        UUID qId = UUID.randomUUID();
        QuestionResult q = new QuestionResult(
                qId, teacherId, UUID.randomUUID(), UUID.randomUUID(),
                8, "Phương trình bậc hai?", "Delta", "MEDIUM", "MULTIPLE_CHOICE", "ACTIVE",
                0, Instant.now(), Instant.now(), List.of(new ChoiceResult(UUID.randomUUID(), "A", null, true, 1))
        );

        when(questionBankUseCase.createQuestion(any())).thenReturn(q);

        Map<String, Object> body = Map.of(
                "content", "Phương trình bậc hai?",
                "difficulty", "MEDIUM",
                "type", "MULTIPLE_CHOICE",
                "grade", 8,
                "choices", List.of(
                        Map.of("content", "Đáp án A", "isCorrect", true, "position", 1),
                        Map.of("content", "Đáp án B", "isCorrect", false, "position", 2)
                )
        );

        mockMvc.perform(post("/api/teacher/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").value("Phương trình bậc hai?"));
    }
}
