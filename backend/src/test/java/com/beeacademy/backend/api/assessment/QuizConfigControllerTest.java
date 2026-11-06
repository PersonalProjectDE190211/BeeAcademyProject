package com.beeacademy.backend.api.assessment;

import com.beeacademy.backend.api.common.GlobalExceptionHandler;
import com.beeacademy.backend.application.assessment.dto.QuizConfigResult;
import com.beeacademy.backend.application.assessment.usecase.ConfigureQuizUseCase;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("QuizConfigController MockMvc Tests")
class QuizConfigControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private ConfigureQuizUseCase configureQuizUseCase;

    private final UUID teacherId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        configureQuizUseCase = Mockito.mock(ConfigureQuizUseCase.class);

        QuizConfigController controller = new QuizConfigController(configureQuizUseCase);

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
    @DisplayName("GET /api/teacher/quiz-configs/chapter/{chapterId} -> Lấy cấu hình quiz của chương")
    void getQuizConfig_Success() throws Exception {
        UUID chapterId = UUID.randomUUID();
        QuizConfigResult config = new QuizConfigResult(
                UUID.randomUUID(), chapterId, teacherId, 10, 3, 5, 2, 45, 80.0, true, true, 2
        );

        when(configureQuizUseCase.getQuizConfig(chapterId)).thenReturn(config);

        mockMvc.perform(get("/api/teacher/quiz-configs/chapter/{chapterId}", chapterId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalQuestions").value(10))
                .andExpect(jsonPath("$.data.passingScore").value(80.0));
    }

    @Test
    @DisplayName("POST /api/teacher/quiz-configs -> Lưu cấu hình quiz")
    void saveQuizConfig_Success() throws Exception {
        UUID chapterId = UUID.randomUUID();
        QuizConfigResult config = new QuizConfigResult(
                UUID.randomUUID(), chapterId, teacherId, 10, 3, 5, 2, 45, 80.0, true, true, 2
        );

        when(configureQuizUseCase.saveQuizConfig(any())).thenReturn(config);

        Map<String, Object> body = Map.of(
                "chapterId", chapterId.toString(),
                "totalQuestions", 10,
                "easyCount", 3,
                "mediumCount", 5,
                "hardCount", 2,
                "timeLimitMinutes", 45,
                "passingScore", 80.0,
                "shuffleQuestions", true,
                "shuffleChoices", true,
                "maxAttempts", 2
        );

        mockMvc.perform(post("/api/teacher/quiz-configs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalQuestions").value(10));
    }
}
