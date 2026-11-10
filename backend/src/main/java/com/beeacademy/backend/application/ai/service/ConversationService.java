package com.beeacademy.backend.application.ai.service;

import com.beeacademy.backend.api.ai.dto.AiChatRequest;
import com.beeacademy.backend.domain.ai.ChatbotIntent;
import com.beeacademy.backend.domain.ai.ConversationContext;
import com.beeacademy.backend.domain.ai.CourseSearchFilter;
import com.beeacademy.backend.infrastructure.ai.persistence.entity.ChatConversationJpaEntity;
import com.beeacademy.backend.infrastructure.ai.persistence.entity.ChatMessageJpaEntity;
import com.beeacademy.backend.infrastructure.ai.persistence.repository.SpringDataChatConversationRepository;
import com.beeacademy.backend.infrastructure.ai.persistence.repository.SpringDataChatMessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

/**
 * Service quản lý vòng đời và lưu trữ trạng thái Hội thoại BeeBot (Conversation Context).
 */
@Service
public class ConversationService {

    private static final Logger log = LoggerFactory.getLogger(ConversationService.class);

    private final SpringDataChatConversationRepository conversationRepository;
    private final SpringDataChatMessageRepository messageRepository;
    private final ObjectMapper objectMapper;

    public ConversationService(SpringDataChatConversationRepository conversationRepository,
                               SpringDataChatMessageRepository messageRepository,
                               ObjectMapper objectMapper) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ConversationContext getOrCreateContext(UUID conversationId, UUID userId) {
        if (conversationId != null) {
            Optional<ChatConversationJpaEntity> found = conversationRepository.findById(conversationId);
            if (found.isPresent()) {
                return toDomainContext(found.get());
            }
        }

        UUID newId = conversationId != null ? conversationId : UUID.randomUUID();
        ChatConversationJpaEntity entity = new ChatConversationJpaEntity(
                newId,
                userId,
                ChatbotIntent.GENERAL_QUESTION.name(),
                "{}",
                new UUID[0],
                Instant.now(),
                Instant.now()
        );
        ChatConversationJpaEntity saved = conversationRepository.save(entity);
        return toDomainContext(saved);
    }

    @Transactional
    public ConversationContext saveContext(ConversationContext context) {
        if (context == null || context.conversationId() == null) {
            return context;
        }

        String filtersJson = "{}";
        try {
            if (context.currentFilters() != null) {
                filtersJson = objectMapper.writeValueAsString(context.currentFilters());
            }
        } catch (Exception e) {
            log.warn("Không thể tuần tự hóa CourseSearchFilter: {}", e.getMessage());
        }

        UUID[] courseIds = context.lastSearchCourseIds() != null
                ? context.lastSearchCourseIds().toArray(new UUID[0])
                : new UUID[0];

        ChatConversationJpaEntity entity = new ChatConversationJpaEntity(
                context.conversationId(),
                context.userId(),
                context.currentIntent() != null ? context.currentIntent().name() : ChatbotIntent.GENERAL_QUESTION.name(),
                filtersJson,
                courseIds,
                Instant.now(),
                Instant.now()
        );

        conversationRepository.save(entity);
        return context;
    }

    @Transactional
    public void recordMessage(UUID conversationId, String role, String content, ChatbotIntent intent, String coursesJson) {
        if (conversationId == null) return;
        ChatMessageJpaEntity msg = new ChatMessageJpaEntity(
                UUID.randomUUID(),
                conversationId,
                role,
                content != null ? content : "",
                intent != null ? intent.name() : null,
                coursesJson != null ? coursesJson : "[]",
                Instant.now()
        );
        messageRepository.save(msg);
    }

    @Transactional(readOnly = true)
    public List<AiChatRequest.ChatMessageDto> getRecentHistory(UUID conversationId, int limit) {
        if (conversationId == null) return Collections.emptyList();
        List<ChatMessageJpaEntity> msgs = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        if (msgs == null || msgs.isEmpty()) return Collections.emptyList();

        int start = Math.max(0, msgs.size() - limit);
        List<AiChatRequest.ChatMessageDto> history = new ArrayList<>();
        for (int i = start; i < msgs.size(); i++) {
            ChatMessageJpaEntity m = msgs.get(i);
            history.add(new AiChatRequest.ChatMessageDto(m.getRole(), m.getContent()));
        }
        return history;
    }

    private ConversationContext toDomainContext(ChatConversationJpaEntity entity) {
        ChatbotIntent intent = ChatbotIntent.fromString(entity.getCurrentIntent());
        CourseSearchFilter filter = CourseSearchFilter.empty();
        try {
            if (entity.getCurrentFiltersJson() != null && !entity.getCurrentFiltersJson().isBlank() && !entity.getCurrentFiltersJson().equals("{}")) {
                filter = objectMapper.readValue(entity.getCurrentFiltersJson(), CourseSearchFilter.class);
            }
        } catch (Exception e) {
            log.warn("Lỗi đọc currentFiltersJson: {}", e.getMessage());
        }

        List<UUID> courseIds = entity.getLastSearchCourseIds() != null
                ? Arrays.asList(entity.getLastSearchCourseIds())
                : Collections.emptyList();

        return new ConversationContext(
                entity.getId(),
                entity.getUserId(),
                intent,
                filter,
                courseIds,
                entity.getUpdatedAt()
        );
    }
}

