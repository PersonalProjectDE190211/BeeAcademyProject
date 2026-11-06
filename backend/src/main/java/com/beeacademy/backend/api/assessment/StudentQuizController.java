package com.beeacademy.backend.api.assessment;

import com.beeacademy.backend.api.assessment.dto.SubmitQuizRequest;
import com.beeacademy.backend.api.common.ApiResponse;
import com.beeacademy.backend.application.assessment.dto.QuizResult;
import com.beeacademy.backend.application.assessment.dto.StartQuizResult;
import com.beeacademy.backend.application.assessment.dto.SubmitQuizCommand;
import com.beeacademy.backend.application.assessment.usecase.TakeQuizUseCase;
import com.beeacademy.backend.platform.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller Không gian làm bài kiểm tra dành cho Học viên (UC17, UC18, UC19).
 */
@Tag(name = "13. Không gian làm bài kiểm tra (Student Quiz Space)", description = "Các API bắt đầu làm bài trắc nghiệm, nhận đề thi xáo trộn, nộp bài tự động tính điểm và xem giải thích chi tiết")
@RestController
@RequestMapping("/api/student/quizzes")
@PreAuthorize("isAuthenticated()")
public class StudentQuizController {

    private final TakeQuizUseCase takeQuizUseCase;

    public StudentQuizController(TakeQuizUseCase takeQuizUseCase) {
        this.takeQuizUseCase = takeQuizUseCase;
    }

    /**
     * UC17: Bắt đầu làm bài kiểm tra của một chương.
     */
    @Operation(summary = "Bắt đầu làm bài kiểm tra", description = "Tạo lượt làm bài thi mới, xáo trộn câu hỏi và phương án, ẩn kết quả đúng")
    @PostMapping("/chapter/{chapterId}/start")
    public ResponseEntity<ApiResponse<StartQuizResult>> startQuiz(@PathVariable UUID chapterId) {
        UUID studentId = SecurityUtils.requireCurrentUserId();
        StartQuizResult result = takeQuizUseCase.startQuiz(studentId, chapterId);
        return ResponseEntity.ok(ApiResponse.success("Bắt đầu bài kiểm tra thành công", result));
    }

    /**
     * UC17, UC18, UC19: Nộp bài thi và nhận kết quả tự động.
     */
    @Operation(summary = "Nộp bài kiểm tra & Chấm điểm tức thì", description = "Tính điểm theo tỷ lệ câu đúng, cập nhật trạng thái đã nộp và trả về chi tiết đáp án")
    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<QuizResult>> submitQuiz(@Valid @RequestBody SubmitQuizRequest request) {
        UUID studentId = SecurityUtils.requireCurrentUserId();
        SubmitQuizCommand command = new SubmitQuizCommand(
                request.attemptId(),
                studentId,
                request.answers()
        );
        QuizResult result = takeQuizUseCase.submitQuiz(command);
        return ResponseEntity.ok(ApiResponse.success("Nộp bài thi thành công", result));
    }
}
