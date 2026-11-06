package com.beeacademy.backend.api.assessment;

import com.beeacademy.backend.api.assessment.dto.SaveQuizConfigRequest;
import com.beeacademy.backend.api.common.ApiResponse;
import com.beeacademy.backend.application.assessment.dto.QuizConfigResult;
import com.beeacademy.backend.application.assessment.dto.SaveQuizConfigCommand;
import com.beeacademy.backend.application.assessment.usecase.ConfigureQuizUseCase;
import com.beeacademy.backend.platform.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller Cấu hình bài kiểm tra chương dành cho Giáo viên (UC33).
 */
@Tag(name = "12. Cấu hình bài kiểm tra (Quiz Configuration)", description = "Các API thiết lập ma trận đề thi Dễ/TB/Khó, thời gian làm bài và điểm chuẩn qua môn")
@RestController
@RequestMapping("/api/teacher/quiz-configs")
@PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
public class QuizConfigController {

    private final ConfigureQuizUseCase configureQuizUseCase;

    public QuizConfigController(ConfigureQuizUseCase configureQuizUseCase) {
        this.configureQuizUseCase = configureQuizUseCase;
    }

    /**
     * Xem cấu hình bài kiểm tra của một chương.
     */
    @Operation(summary = "Xem cấu hình bài kiểm tra của chương")
    @GetMapping("/chapter/{chapterId}")
    public ResponseEntity<ApiResponse<QuizConfigResult>> getQuizConfig(@PathVariable UUID chapterId) {
        QuizConfigResult result = configureQuizUseCase.getQuizConfig(chapterId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Lưu hoặc cập nhật cấu hình bài kiểm tra của một chương.
     */
    @Operation(summary = "Lưu cấu hình bài kiểm tra", description = "Kiểm tra chặt chẽ quy tắc: tổng số câu dễ + TB + khó phải bằng đúng tổng số câu hỏi")
    @PostMapping
    public ResponseEntity<ApiResponse<QuizConfigResult>> saveQuizConfig(@Valid @RequestBody SaveQuizConfigRequest request) {
        UUID teacherId = SecurityUtils.requireCurrentUserId();

        SaveQuizConfigCommand command = new SaveQuizConfigCommand(
                request.chapterId(),
                teacherId,
                request.totalQuestions(),
                request.easyCount(),
                request.mediumCount(),
                request.hardCount(),
                request.timeLimitMinutes(),
                request.passingScore(),
                request.shuffleQuestions(),
                request.shuffleChoices(),
                request.maxAttempts()
        );

        QuizConfigResult result = configureQuizUseCase.saveQuizConfig(command);
        return ResponseEntity.ok(ApiResponse.success("Lưu cấu hình bài kiểm tra thành công", result));
    }
}
