package com.beeacademy.backend.infrastructure.ai.persistence.repository;

import com.beeacademy.backend.infrastructure.ai.persistence.entity.ChatMessageJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataChatMessageRepository extends JpaRepository<ChatMessageJpaEntity, UUID> {
    List<ChatMessageJpaEntity> findByConversationIdOrderByCreatedAtAsc(UUID conversationId);
}

