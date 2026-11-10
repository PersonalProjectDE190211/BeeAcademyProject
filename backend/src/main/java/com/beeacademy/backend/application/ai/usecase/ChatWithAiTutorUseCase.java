package com.beeacademy.backend.application.ai.usecase;

import com.beeacademy.backend.api.ai.dto.AiChatRequest;
import com.beeacademy.backend.api.ai.dto.AiChatResponse;
import com.beeacademy.backend.api.ai.dto.ChatbotMessageResponse;
import com.beeacademy.backend.application.ai.service.ChatbotService;
import org.springframework.stereotype.Service;

/**
 * Use Case UC22: Trợ lý AI BeeBot - Tư vấn tuyển sinh & khóa học K-12.
 */
@Service
public class ChatWithAiTutorUseCase {

    private final ChatbotService chatbotService;

    public ChatWithAiTutorUseCase(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    public AiChatResponse execute(AiChatRequest request) {
        ChatbotMessageResponse result = chatbotService.processMessage(
                request.conversationId(),
                null, // Anonymous or authenticated user ID
                request.message()
        );

        return new AiChatResponse(
                result.reply(),
                result.suggestions(),
                result.conversationId(),
                result.intent(),
                result.courses()
        );
    }
}
