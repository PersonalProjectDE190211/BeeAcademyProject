package com.beeacademy.backend.api.learning;

import com.beeacademy.backend.api.common.ApiResponse;
import com.beeacademy.backend.api.learning.dto.TrackProgressRequest;
import com.beeacademy.backend.application.learning.dto.EnrollmentSummaryResult;
import com.beeacademy.backend.application.learning.dto.LearningLessonResult;
import com.beeacademy.backend.application.learning.dto.LessonProgressResult;
import com.beeacademy.backend.application.learning.dto.TrackProgressCommand;
import com.beeacademy.backend.application.learning.usecase.GetLearningLessonUseCase;
import com.beeacademy.backend.application.learning.usecase.GetMyEnrollmentsUseCase;
import com.beeacademy.backend.application.learning.usecase.TrackLessonProgressUseCase;
import com.beeacademy.backend.platform.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller Không gian Học tập & Theo dõi tiến độ bài giảng (UC13, UC14, UC15).
 */
@Tag(name = "8. Không gian Học tập (Learning Space)", description = "Các API xem khóa học đã mua, nội dung bài học và ghi nhận tiến độ xem video chống tua")
@RestController
@RequestMapping("/api/learning")
@PreAuthorize("isAuthenticated()")
public class LearningController {

    private final GetMyEnrollmentsUseCase getMyEnrollmentsUseCase;
    private final GetLearningLessonUseCase getLearningLessonUseCase;
    private final TrackLessonProgressUseCase trackLessonProgressUseCase;

    public LearningController(GetMyEnrollmentsUseCase getMyEnrollmentsUseCase,
                              GetLearningLessonUseCase getLearningLessonUseCase,
                              TrackLessonProgressUseCase trackLessonProgressUseCase) {
        this.getMyEnrollmentsUseCase = getMyEnrollmentsUseCase;
        this.getLearningLessonUseCase = getLearningLessonUseCase;
        this.trackLessonProgressUseCase = trackLessonProgressUseCase;
    }

    /**
     * UC13: Lấy danh sách khóa học học viên đang theo học.
     */
    @Operation(summary = "Khóa học của tôi (My Courses)", description = "Danh sách các khóa học đã đăng ký kèm phần trăm tiến độ hoàn thành")
    @GetMapping("/my-courses")
    public ResponseEntity<ApiResponse<List<EnrollmentSummaryResult>>> getMyCourses() {
        UUID studentId = SecurityUtils.requireCurrentUserId();
        List<EnrollmentSummaryResult> enrollments = getMyEnrollmentsUseCase.execute(studentId);
        return ResponseEntity.ok(ApiResponse.success(enrollments));
    }

    /**
     * UC14: Lấy thông tin bài học và link video streaming trong phòng học.
     */
    @Operation(summary = "Xem nội dung bài học trong phòng học", description = "Lấy link streaming video, thời lượng và vị trí đã học dở")
    @GetMapping("/courses/{courseId}/lessons/{lessonId}")
    public ResponseEntity<ApiResponse<LearningLessonResult>> getLearningLesson(
            @PathVariable UUID courseId,
            @PathVariable UUID lessonId
    ) {
        UUID studentId = SecurityUtils.requireCurrentUserId();
        LearningLessonResult result = getLearningLessonUseCase.execute(studentId, courseId, lessonId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * UC14, UC15: Gửi heartbeat phân đoạn video đã xem để cập nhật tiến độ chống tua gian lận.
     */
    @Operation(summary = "Cập nhật tiến độ xem video (Anti-Skip Heartbeat)", description = "Định kỳ gửi khoảng thời gian đã xem [start, end]. Khi đạt >= 80%, bài học tự động đánh dấu hoàn thành")
    @PostMapping("/courses/{courseId}/lessons/{lessonId}/progress")
    public ResponseEntity<ApiResponse<LessonProgressResult>> trackProgress(
            @PathVariable UUID courseId,
            @PathVariable UUID lessonId,
            @Valid @RequestBody TrackProgressRequest request
    ) {
        UUID studentId = SecurityUtils.requireCurrentUserId();
        TrackProgressCommand command = new TrackProgressCommand(
                studentId,
                courseId,
                lessonId,
                request.startSec(),
                request.endSec(),
                request.currentPositionSec()
        );
        LessonProgressResult result = trackLessonProgressUseCase.execute(command);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật tiến độ thành công", result));
    }
}
