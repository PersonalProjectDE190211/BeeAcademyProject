package com.beeacademy.backend.domain.ai;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Trạng thái ngữ cảnh của phiên hội thoại (Conversation Context).
 */
public record ConversationContext(
        UUID conversationId,
        UUID userId,
        ChatbotIntent currentIntent,
        CourseSearchFilter currentFilters,
        List<UUID> lastSearchCourseIds,
        Instant updatedAt
) {

    public static ConversationContext init(UUID conversationId, UUID userId) {
        return new ConversationContext(
                conversationId != null ? conversationId : UUID.randomUUID(),
                userId,
                ChatbotIntent.GENERAL_QUESTION,
                CourseSearchFilter.empty(),
                Collections.emptyList(),
                Instant.now()
        );
    }

    public ConversationContext withUpdatedState(ChatbotIntent intent, CourseSearchFilter filters, List<UUID> courseIds) {
        return new ConversationContext(
                this.conversationId,
                this.userId,
                intent != null ? intent : this.currentIntent,
                filters != null ? filters : this.currentFilters,
                courseIds != null ? new ArrayList<>(courseIds) : this.lastSearchCourseIds,
                Instant.now()
        );
    }
}

