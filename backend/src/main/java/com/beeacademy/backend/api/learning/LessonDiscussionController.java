package com.beeacademy.backend.api.learning;

import com.beeacademy.backend.api.common.ApiResponse;
import com.beeacademy.backend.api.learning.dto.AskQuestionRequest;
import com.beeacademy.backend.api.learning.dto.ReplyQuestionRequest;
import com.beeacademy.backend.application.learning.dto.AskQuestionCommand;
import com.beeacademy.backend.application.learning.dto.QaThreadResult;
import com.beeacademy.backend.application.learning.dto.ReplyQuestionCommand;
import com.beeacademy.backend.application.learning.usecase.ManageLessonDiscussionUseCase;
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
 * Controller Hỏi đáp Q&A dưới bài giảng (UC21, UC36).
 */
@Tag(name = "10. Thảo luận & Hỏi đáp bài giảng (Q&A)", description = "Các API đặt câu hỏi dưới bài giảng và giáo viên giải đáp thắc mắc")
@RestController
@RequestMapping("/api/learning")
@PreAuthorize("isAuthenticated()")
public class LessonDiscussionController {

    private final ManageLessonDiscussionUseCase discussionUseCase;

    public LessonDiscussionController(ManageLessonDiscussionUseCase discussionUseCase) {
        this.discussionUseCase = discussionUseCase;
    }

    /**
     * Xem danh sách các câu hỏi thảo luận dưới bài giảng.
     */
    @Operation(summary = "Danh sách câu hỏi bài học")
    @GetMapping("/lessons/{lessonId}/discussions")
    public ResponseEntity<ApiResponse<List<QaThreadResult>>> getDiscussions(@PathVariable UUID lessonId) {
        List<QaThreadResult> discussions = discussionUseCase.getLessonDiscussions(lessonId);
        return ResponseEntity.ok(ApiResponse.success(discussions));
    }

    /**
     * UC21: Học viên đặt câu hỏi mới dưới bài giảng.
     */
    @Operation(summary = "Đặt câu hỏi mới")
    @PostMapping("/courses/{courseId}/lessons/{lessonId}/discussions")
    public ResponseEntity<ApiResponse<QaThreadResult>> askQuestion(
            @PathVariable UUID courseId,
            @PathVariable UUID lessonId,
            @Valid @RequestBody AskQuestionRequest request
    ) {
        UUID studentId = SecurityUtils.requireCurrentUserId();
        AskQuestionCommand command = new AskQuestionCommand(
                courseId,
                lessonId,
                studentId,
                request.title(),
                request.content()
        );
        QaThreadResult result = discussionUseCase.askQuestion(command);
        return ResponseEntity.ok(ApiResponse.success("Gửi câu hỏi thành công", result));
    }

    /**
     * UC36: Giáo viên hoặc bạn học gửi câu trả lời phản hồi.
     */
    @Operation(summary = "Gửi câu trả lời phản hồi")
    @PostMapping("/discussions/{threadId}/replies")
    public ResponseEntity<ApiResponse<QaThreadResult>> replyQuestion(
            @PathVariable UUID threadId,
            @Valid @RequestBody ReplyQuestionRequest request
    ) {
        UUID senderId = SecurityUtils.requireCurrentUserId();
        ReplyQuestionCommand command = new ReplyQuestionCommand(
                threadId,
                senderId,
                request.content()
        );
        QaThreadResult result = discussionUseCase.replyQuestion(command);
        return ResponseEntity.ok(ApiResponse.success("Gửi phản hồi thành công", result));
    }
}
