package com.beeacademy.backend.api.ai.dto;

import java.util.List;
import java.util.UUID;

public record ChatbotMessageResponse(
        UUID conversationId,
        String reply,
        String intent,
        List<ChatbotCourseCardDto> courses,
        List<String> suggestions
) {
}

