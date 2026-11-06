package com.beeacademy.backend.api.learning;

import com.beeacademy.backend.api.common.GlobalExceptionHandler;
import com.beeacademy.backend.application.learning.dto.QaThreadResult;
import com.beeacademy.backend.application.learning.usecase.ManageLessonDiscussionUseCase;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("LessonDiscussionController MockMvc Tests")
class LessonDiscussionControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private ManageLessonDiscussionUseCase discussionUseCase;

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        discussionUseCase = Mockito.mock(ManageLessonDiscussionUseCase.class);

        LessonDiscussionController controller = new LessonDiscussionController(discussionUseCase);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        UserPrincipal principal = new UserPrincipal(userId, "student@test.com", "STUDENT");
        var auth = new UsernamePasswordAuthenticationToken(principal, null, List.of(new SimpleGrantedAuthority("ROLE_STUDENT")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("GET /api/learning/lessons/{lessonId}/discussions -> Trả về danh sách câu hỏi thảo luận")
    void getDiscussions_Success() throws Exception {
        UUID lessonId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        QaThreadResult thread = new QaThreadResult(
                UUID.randomUUID(), courseId, lessonId, userId, "Hỏi về vòng lặp", "Nội dung câu hỏi",
                false, Instant.now(), Instant.now(), Collections.emptyList()
        );

        when(discussionUseCase.getLessonDiscussions(lessonId)).thenReturn(List.of(thread));

        mockMvc.perform(get("/api/learning/lessons/{lessonId}/discussions", lessonId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("Hỏi về vòng lặp"));
    }

    @Test
    @DisplayName("POST /api/learning/courses/{courseId}/lessons/{lessonId}/discussions -> Đặt câu hỏi mới")
    void askQuestion_Success() throws Exception {
        UUID courseId = UUID.randomUUID();
        UUID lessonId = UUID.randomUUID();
        QaThreadResult thread = new QaThreadResult(
                UUID.randomUUID(), courseId, lessonId, userId, "Cách dùng switch-case mới", "Nội dung",
                false, Instant.now(), Instant.now(), Collections.emptyList()
        );

        when(discussionUseCase.askQuestion(any())).thenReturn(thread);

        Map<String, String> body = Map.of(
                "title", "Cách dùng switch-case mới",
                "content", "Xin hỏi cú pháp switch-case pattern matching trong Java 21?"
        );

        mockMvc.perform(post("/api/learning/courses/{courseId}/lessons/{lessonId}/discussions", courseId, lessonId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Cách dùng switch-case mới"));
    }

    @Test
    @DisplayName("POST /api/learning/discussions/{threadId}/replies -> Trả lời câu hỏi")
    void replyQuestion_Success() throws Exception {
        UUID threadId = UUID.randomUUID();
        QaThreadResult thread = new QaThreadResult(
                threadId, UUID.randomUUID(), UUID.randomUUID(), userId, "Hỏi đáp Java", "Nội dung",
                true, Instant.now(), Instant.now(), Collections.emptyList()
        );

        when(discussionUseCase.replyQuestion(any())).thenReturn(thread);

        Map<String, String> body = Map.of("content", "Cú pháp pattern matching rất tiện lợi!");

        mockMvc.perform(post("/api/learning/discussions/{threadId}/replies", threadId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
