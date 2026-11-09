package com.beeacademy.backend.api.learning;

import com.beeacademy.backend.api.common.GlobalExceptionHandler;
import com.beeacademy.backend.application.learning.dto.EnrollmentSummaryResult;
import com.beeacademy.backend.application.learning.dto.LearningLessonResult;
import com.beeacademy.backend.application.learning.dto.LessonProgressResult;
import com.beeacademy.backend.application.learning.usecase.GetLearningLessonUseCase;
import com.beeacademy.backend.application.learning.usecase.GetMyEnrollmentsUseCase;
import com.beeacademy.backend.application.learning.usecase.TrackLessonProgressUseCase;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("LearningController MockMvc Tests")
class LearningControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private GetMyEnrollmentsUseCase getMyEnrollmentsUseCase;
    private GetLearningLessonUseCase getLearningLessonUseCase;
    private TrackLessonProgressUseCase trackLessonProgressUseCase;

    private final UUID studentId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        getMyEnrollmentsUseCase = Mockito.mock(GetMyEnrollmentsUseCase.class);
        getLearningLessonUseCase = Mockito.mock(GetLearningLessonUseCase.class);
        trackLessonProgressUseCase = Mockito.mock(TrackLessonProgressUseCase.class);

        LearningController controller = new LearningController(
                getMyEnrollmentsUseCase, getLearningLessonUseCase, trackLessonProgressUseCase
        );

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
    @DisplayName("GET /api/learning/my-courses -> Trả về danh sách khóa học của học viên")
    void getMyCourses_Success() throws Exception {
        UUID courseId = UUID.randomUUID();
        EnrollmentSummaryResult summary = new EnrollmentSummaryResult(
                UUID.randomUUID(), courseId, "Khóa học Java", "khoa-hoc-java",
                "https://thumb.jpg", 5, 10, 50.0, false, Instant.now(), null
        );

        when(getMyEnrollmentsUseCase.execute(studentId)).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/learning/my-courses")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].courseTitle").value("Khóa học Java"))
                .andExpect(jsonPath("$.data[0].progressPercent").value(50.0));
    }

    @Test
    @DisplayName("GET /api/learning/courses/{courseId}/lessons/{lessonId} -> Lấy nội dung bài học")
    void getLearningLesson_Success() throws Exception {
        UUID courseId = UUID.randomUUID();
        UUID lessonId = UUID.randomUUID();
        UUID chapterId = UUID.randomUUID();

        LearningLessonResult result = new LearningLessonResult(
                lessonId, chapterId, "Bài 1: Giới thiệu", 1,
                "https://video.mp4", 600, false, false, 120, 20.0,
                List.of()
        );

        when(getLearningLessonUseCase.execute(studentId, courseId, lessonId)).thenReturn(result);

        mockMvc.perform(get("/api/learning/courses/{courseId}/lessons/{lessonId}", courseId, lessonId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Bài 1: Giới thiệu"))
                .andExpect(jsonPath("$.data.durationSec").value(600));
    }

    @Test
    @DisplayName("POST /api/learning/courses/{courseId}/lessons/{lessonId}/progress -> Ghi nhận heartbeat tiến độ")
    void trackProgress_Success() throws Exception {
        UUID courseId = UUID.randomUUID();
        UUID lessonId = UUID.randomUUID();

        LessonProgressResult result = new LessonProgressResult(
                lessonId, courseId, 250, 250, 41.6, false, null, 10.0, false
        );

        when(trackLessonProgressUseCase.execute(any())).thenReturn(result);

        Map<String, Object> body = Map.of(
                "startSec", 0,
                "endSec", 250,
                "currentPositionSec", 250
        );

        mockMvc.perform(post("/api/learning/courses/{courseId}/lessons/{lessonId}/progress", courseId, lessonId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.watchedSeconds").value(250))
                .andExpect(jsonPath("$.data.isCompleted").value(false));
    }
}
