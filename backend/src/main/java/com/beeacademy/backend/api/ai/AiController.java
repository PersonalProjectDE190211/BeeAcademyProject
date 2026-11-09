package com.beeacademy.backend.api.ai;

import com.beeacademy.backend.api.ai.dto.AiChatRequest;
import com.beeacademy.backend.api.ai.dto.AiChatResponse;
import com.beeacademy.backend.api.common.ApiResponse;
import com.beeacademy.backend.application.ai.usecase.ChatWithAiTutorUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller Trợ lý Tư vấn Tuyển sinh & Khóa học BeeBot (UC22).
 */
@Tag(name = "12. Trợ lý Tư vấn Tuyển sinh & Khóa học BeeBot (UC22)", description = "API tư vấn chọn khóa học K-12, giải đáp học phí và giải đáp thắc mắc cho Phụ huynh & Học sinh")
@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final ChatWithAiTutorUseCase chatWithAiTutorUseCase;

    public AiController(ChatWithAiTutorUseCase chatWithAiTutorUseCase) {
        this.chatWithAiTutorUseCase = chatWithAiTutorUseCase;
    }

    /**
     * UC22: Trò chuyện và nhận tư vấn khóa học từ BeeBot.
     */
    @Operation(summary = "Tư vấn khóa học với BeeBot", description = "Giải đáp thắc mắc, tư vấn chọn khóa học phù hợp theo khối lớp, học lực và học phí")
    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<AiChatResponse>> chat(@Valid @RequestBody AiChatRequest request) {
        AiChatResponse response = chatWithAiTutorUseCase.execute(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

