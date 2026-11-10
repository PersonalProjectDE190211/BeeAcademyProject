package com.beeacademy.backend.api.ai;

import com.beeacademy.backend.api.ai.dto.ChatbotMessageRequest;
import com.beeacademy.backend.api.ai.dto.ChatbotMessageResponse;
import com.beeacademy.backend.api.common.ApiResponse;
import com.beeacademy.backend.application.ai.service.ChatbotService;
import com.beeacademy.backend.platform.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller Chatbot tư vấn khóa học thông minh BeeBot (Kiến trúc 4 giai đoạn).
 */
@Tag(name = "12. Trợ lý Tư vấn Tuyển sinh & Khóa học BeeBot (UC22)", description = "API điều phối tư vấn khóa học K-12, phân tích intent và quản lý trạng thái hội thoại")
@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    /**
     * Nhận câu hỏi, phân tích intent, truy vấn database và sinh câu trả lời tư vấn kèm Course Cards.
     */
    @Operation(summary = "Gửi tin nhắn tư vấn tới BeeBot", description = "Quản lý ngữ cảnh hội thoại, cập nhật điều kiện tìm kiếm và trả kết quả khóa học chuẩn xác")
    @PostMapping("/message")
    public ResponseEntity<ApiResponse<ChatbotMessageResponse>> sendMessage(@Valid @RequestBody ChatbotMessageRequest request) {
        UUID currentUserId = SecurityUtils.getCurrentUserId().orElse(null);
        ChatbotMessageResponse response = chatbotService.processMessage(
                request.conversationId(),
                currentUserId,
                request.message()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

