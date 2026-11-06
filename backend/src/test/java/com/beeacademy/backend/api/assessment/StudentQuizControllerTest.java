package com.beeacademy.backend.api.assessment;

import com.beeacademy.backend.api.common.GlobalExceptionHandler;
import com.beeacademy.backend.application.assessment.dto.QuizResult;
import com.beeacademy.backend.application.assessment.dto.StartQuizResult;
import com.beeacademy.backend.application.assessment.usecase.TakeQuizUseCase;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("StudentQuizController MockMvc Tests")
class StudentQuizControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private TakeQuizUseCase takeQuizUseCase;

    private final UUID studentId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        takeQuizUseCase = Mockito.mock(TakeQuizUseCase.class);

        StudentQuizController controller = new StudentQuizController(takeQuizUseCase);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        UserPrincipal principal = new UserPrincipal(studentId, "student@test.com", "STUDENT");
        var auth = new UsernamePasswordAuthenticationToken(principal, null, List.of(new SimpleGrantedAuthority("ROLE_STUDENT")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("POST /api/student/quizzes/chapter/{chapterId}/start -> Bắt đầu làm bài thi")
    void startQuiz_Success() throws Exception {
        UUID chapterId = UUID.randomUUID();
        UUID attemptId = UUID.randomUUID();

        StartQuizResult startResult = new StartQuizResult(
                attemptId, chapterId, 10, 45, 80.0, Instant.now(), Collections.emptyList()
        );

        when(takeQuizUseCase.startQuiz(studentId, chapterId)).thenReturn(startResult);

        mockMvc.perform(post("/api/student/quizzes/chapter/{chapterId}/start", chapterId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.attemptId").value(attemptId.toString()))
                .andExpect(jsonPath("$.data.totalQuestions").value(10));
    }

    @Test
    @DisplayName("POST /api/student/quizzes/submit -> Nộp bài thi và nhận điểm")
    void submitQuiz_Success() throws Exception {
        UUID attemptId = UUID.randomUUID();
        UUID chapterId = UUID.randomUUID();

        QuizResult quizResult = new QuizResult(
                attemptId, studentId, chapterId, 100.0, true, 10, 10, Instant.now(), Collections.emptyList()
        );

        when(takeQuizUseCase.submitQuiz(any())).thenReturn(quizResult);

        Map<String, Object> body = Map.of(
                "attemptId", attemptId.toString(),
                "answers", Map.of()
        );

        mockMvc.perform(post("/api/student/quizzes/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.score").value(100.0))
                .andExpect(jsonPath("$.data.passed").value(true));
    }
}
