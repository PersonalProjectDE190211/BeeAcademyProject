package com.beeacademy.backend.application.learning.dto;

import java.util.UUID;

public record ReplyQuestionCommand(
        UUID threadId,
        UUID senderId,
        String content
) {
}
