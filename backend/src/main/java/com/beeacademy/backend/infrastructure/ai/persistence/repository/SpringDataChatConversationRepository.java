package com.beeacademy.backend.infrastructure.ai.persistence.repository;

import com.beeacademy.backend.infrastructure.ai.persistence.entity.ChatConversationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataChatConversationRepository extends JpaRepository<ChatConversationJpaEntity, UUID> {
    Optional<ChatConversationJpaEntity> findById(UUID id);
}

