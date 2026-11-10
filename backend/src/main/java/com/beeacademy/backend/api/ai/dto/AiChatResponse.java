package com.beeacademy.backend.api.ai.dto;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public record AiChatResponse(
        String reply,
        List<String> suggestions,
        UUID conversationId,
        String intent,
        List<ChatbotCourseCardDto> courses
) {
    public AiChatResponse(String reply, List<String> suggestions) {
        this(reply, suggestions, null, null, Collections.emptyList());
    }
}
