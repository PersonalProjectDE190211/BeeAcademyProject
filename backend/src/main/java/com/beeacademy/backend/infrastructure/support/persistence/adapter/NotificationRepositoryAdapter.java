package com.beeacademy.backend.infrastructure.support.persistence.adapter;

import com.beeacademy.backend.domain.support.AdminNotification;
import com.beeacademy.backend.domain.support.NotificationRepositoryPort;
import com.beeacademy.backend.domain.support.NotificationType;
import com.beeacademy.backend.domain.support.UserNotification;
import com.beeacademy.backend.infrastructure.support.persistence.entity.NotificationJpaEntity;
import com.beeacademy.backend.infrastructure.support.persistence.repository.SpringDataNotificationRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class NotificationRepositoryAdapter implements NotificationRepositoryPort {

    private final SpringDataNotificationRepository notificationRepository;

    public NotificationRepositoryAdapter(SpringDataNotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional
    public AdminNotification saveAdminNotification(AdminNotification notification) {
        NotificationJpaEntity entity = new NotificationJpaEntity(
                notification.getId(),
                null,
                "admin",
                notification.getTitle(),
                notification.getMessage(),
                notification.getType().name().toLowerCase(),
                notification.getActionUrl(),
                notification.isRead(),
                notification.getReadAt(),
                notification.getCreatedAt()
        );
        NotificationJpaEntity saved = notificationRepository.save(entity);
        return toAdminDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AdminNotification> findAdminNotificationById(UUID id) {
        return notificationRepository.findById(id).map(this::toAdminDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminNotification> findUnreadAdminNotifications() {
        return notificationRepository.findByTargetRoleAndIsReadFalseOrderByCreatedAtDesc("admin").stream()
                .map(this::toAdminDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminNotification> findAllAdminNotifications() {
        return notificationRepository.findByTargetRoleOrderByCreatedAtDesc("admin").stream()
                .map(this::toAdminDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserNotification saveUserNotification(UserNotification notification) {
        NotificationJpaEntity entity = new NotificationJpaEntity(
                notification.getId(),
                notification.getRecipientId(),
                null,
                notification.getTitle(),
                notification.getMessage(),
                notification.getType().name().toLowerCase(),
                notification.getActionUrl(),
                notification.isRead(),
                notification.getReadAt(),
                notification.getCreatedAt()
        );
        NotificationJpaEntity saved = notificationRepository.save(entity);
        return toUserDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserNotification> findUserNotificationById(UUID id) {
        return notificationRepository.findById(id).map(this::toUserDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserNotification> findByRecipientId(UUID recipientId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId).stream()
                .map(this::toUserDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserNotification> findUnreadByRecipientId(UUID recipientId) {
        return notificationRepository.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(recipientId).stream()
                .map(this::toUserDomain)
                .collect(Collectors.toList());
    }

    private AdminNotification toAdminDomain(NotificationJpaEntity entity) {
        NotificationType type;
        try {
            type = NotificationType.valueOf(entity.getType().toUpperCase());
        } catch (Exception e) {
            type = NotificationType.SYSTEM_ALERT;
        }

        return new AdminNotification(
                entity.getId(),
                entity.getTitle(),
                entity.getMessage(),
                type,
                entity.isRead(),
                entity.getActionUrl(),
                entity.getCreatedAt(),
                entity.getReadAt()
        );
    }

    private UserNotification toUserDomain(NotificationJpaEntity entity) {
        NotificationType type;
        try {
            type = NotificationType.valueOf(entity.getType().toUpperCase());
        } catch (Exception e) {
            type = NotificationType.GENERAL;
        }

        return new UserNotification(
                entity.getId(),
                entity.getRecipientId(),
                entity.getTitle(),
                entity.getMessage(),
                type,
                entity.isRead(),
                entity.getActionUrl(),
                entity.getCreatedAt(),
                entity.getReadAt()
        );
    }
}

