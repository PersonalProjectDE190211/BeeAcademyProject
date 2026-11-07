package com.beeacademy.backend.infrastructure.support.persistence.repository;

import com.beeacademy.backend.infrastructure.support.persistence.entity.NotificationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataNotificationRepository extends JpaRepository<NotificationJpaEntity, UUID> {

    List<NotificationJpaEntity> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId);

    List<NotificationJpaEntity> findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(UUID recipientId);

    List<NotificationJpaEntity> findByTargetRoleOrderByCreatedAtDesc(String targetRole);

    List<NotificationJpaEntity> findByTargetRoleAndIsReadFalseOrderByCreatedAtDesc(String targetRole);
}

