package com.beeacademy.backend.api.assessment;

import com.beeacademy.backend.api.assessment.dto.ChoiceItemRequest;
import com.beeacademy.backend.api.assessment.dto.CreateQuestionRequest;
import com.beeacademy.backend.api.common.ApiResponse;
import com.beeacademy.backend.application.assessment.dto.ChoiceItem;
import com.beeacademy.backend.application.assessment.dto.CreateQuestionCommand;
import com.beeacademy.backend.application.assessment.dto.QuestionResult;
import com.beeacademy.backend.application.assessment.usecase.ManageQuestionBankUseCase;
import com.beeacademy.backend.platform.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Controller Quản lý Ngân hàng câu hỏi dành cho Giáo viên (UC34).
 */
@Tag(name = "11. Ngân hàng câu hỏi (Question Bank)", description = "Các API tạo câu hỏi, phân loại độ khó, gán nhãn tags và quản lý phiên bản")
@RestController
@RequestMapping("/api/teacher/questions")
@PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
public class QuestionBankController {

    private final ManageQuestionBankUseCase questionBankUseCase;

    public QuestionBankController(ManageQuestionBankUseCase questionBankUseCase) {
        this.questionBankUseCase = questionBankUseCase;
    }

    /**
     * UC34: Tạo mới một câu hỏi vào ngân hàng.
     */
    @Operation(summary = "Tạo câu hỏi mới", description = "Tạo câu hỏi trắc nghiệm hoặc đúng/sai kèm danh sách phương án lựa chọn và giải thích")
    @PostMapping
    public ResponseEntity<ApiResponse<QuestionResult>> createQuestion(@Valid @RequestBody CreateQuestionRequest request) {
        UUID teacherId = SecurityUtils.requireCurrentUserId();

        List<ChoiceItem> choices = new ArrayList<>();
        if (request.choices() != null) {
            for (ChoiceItemRequest c : request.choices()) {
                choices.add(new ChoiceItem(c.content(), c.imageUrl(), c.isCorrect(), c.position()));
            }
        }

        CreateQuestionCommand command = new CreateQuestionCommand(
                teacherId,
                request.categoryId(),
                request.chapterId(),
                request.grade(),
                request.content(),
                request.explanation(),
                request.difficulty(),
                request.type(),
                choices,
                request.tags()
        );

        QuestionResult result = questionBankUseCase.createQuestion(command);
        return ResponseEntity.ok(ApiResponse.success("Tạo câu hỏi thành công", result));
    }

    /**
     * Lấy danh sách câu hỏi do giáo viên đang đăng nhập phụ trách.
     */
    @Operation(summary = "Danh sách câu hỏi của giáo viên")
    @GetMapping
    public ResponseEntity<ApiResponse<List<QuestionResult>>> getMyQuestions() {
        UUID teacherId = SecurityUtils.requireCurrentUserId();
        List<QuestionResult> questions = questionBankUseCase.getQuestionsByTeacher(teacherId);
        return ResponseEntity.ok(ApiResponse.success(questions));
    }

    /**
     * Lấy danh sách câu hỏi trong một chương học.
     */
    @Operation(summary = "Danh sách câu hỏi theo chương")
    @GetMapping("/chapter/{chapterId}")
    public ResponseEntity<ApiResponse<List<QuestionResult>>> getQuestionsByChapter(@PathVariable UUID chapterId) {
        List<QuestionResult> questions = questionBankUseCase.getQuestionsByChapter(chapterId);
        return ResponseEntity.ok(ApiResponse.success(questions));
    }

    /**
     * Xem chi tiết một câu hỏi theo ID.
     */
    @Operation(summary = "Chi tiết câu hỏi")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QuestionResult>> getQuestionDetail(@PathVariable UUID id) {
        QuestionResult question = questionBankUseCase.getQuestionDetail(id);
        return ResponseEntity.ok(ApiResponse.success(question));
    }

    /**
     * Vô hiệu hóa một câu hỏi khỏi ngân hàng.
     */
    @Operation(summary = "Vô hiệu hóa câu hỏi")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivateQuestion(
            @PathVariable UUID id,
            @RequestParam(required = false, defaultValue = "Vô hiệu hóa câu hỏi") String reason
    ) {
        UUID teacherId = SecurityUtils.requireCurrentUserId();
        questionBankUseCase.deactivateQuestion(id, teacherId, reason);
        return ResponseEntity.ok(ApiResponse.success("Đã vô hiệu hóa câu hỏi thành công", null));
    }
}
