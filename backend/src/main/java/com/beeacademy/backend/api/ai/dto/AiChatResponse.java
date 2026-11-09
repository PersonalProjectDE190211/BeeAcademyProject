package com.beeacademy.backend.api.ai.dto;

import java.util.List;

public record AiChatResponse(
        String reply,
        List<String> suggestions
) {
}

