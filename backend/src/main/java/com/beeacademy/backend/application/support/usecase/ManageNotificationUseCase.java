package com.beeacademy.backend.application.support.usecase;

import com.beeacademy.backend.application.support.dto.NotificationResult;
import com.beeacademy.backend.application.support.dto.SendNotificationCommand;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.support.AdminNotification;
import com.beeacademy.backend.domain.support.NotificationRepositoryPort;
import com.beeacademy.backend.domain.support.UserNotification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ManageNotificationUseCase {

    private final NotificationRepositoryPort notificationRepository;

    public ManageNotificationUseCase(NotificationRepositoryPort notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional(readOnly = true)
    public List<NotificationResult> getUserNotifications(UUID recipientId) {
        return notificationRepository.findByRecipientId(recipientId).stream()
                .map(this::toUserResult)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NotificationResult> getUnreadUserNotifications(UUID recipientId) {
        return notificationRepository.findUnreadByRecipientId(recipientId).stream()
                .map(this::toUserResult)
                .collect(Collectors.toList());
    }

    @Transactional
    public NotificationResult markAsRead(UUID notificationId, UUID recipientId) {
        UserNotification notification = notificationRepository.findUserNotificationById(notificationId)
                .orElseThrow(() -> new DomainException("NOTIFICATION_NOT_FOUND", "Không tìm thấy thông báo"));

        if (!notification.getRecipientId().equals(recipientId)) {
            throw new DomainException("FORBIDDEN", "Bạn không có quyền thao tác trên thông báo này");
        }

        notification.markAsRead();
        UserNotification saved = notificationRepository.saveUserNotification(notification);
        return toUserResult(saved);
    }

    @Transactional
    public NotificationResult sendNotification(SendNotificationCommand command) {
        UserNotification notification = UserNotification.dispatch(
                command.recipientId(),
                command.title(),
                command.message(),
                command.type(),
                command.actionUrl()
        );

        UserNotification saved = notificationRepository.saveUserNotification(notification);
        return toUserResult(saved);
    }

    @Transactional(readOnly = true)
    public List<NotificationResult> getAdminNotifications(boolean unreadOnly) {
        List<AdminNotification> notifications = unreadOnly
                ? notificationRepository.findUnreadAdminNotifications()
                : notificationRepository.findAllAdminNotifications();

        return notifications.stream()
                .map(this::toAdminResult)
                .collect(Collectors.toList());
    }

    private NotificationResult toUserResult(UserNotification n) {
        return new NotificationResult(
                n.getId(),
                n.getRecipientId(),
                n.getTitle(),
                n.getMessage(),
                n.getType().name(),
                n.getActionUrl(),
                n.isRead(),
                n.getCreatedAt(),
                n.getReadAt()
        );
    }

    private NotificationResult toAdminResult(AdminNotification n) {
        return new NotificationResult(
                n.getId(),
                null,
                n.getTitle(),
                n.getMessage(),
                n.getType().name(),
                n.getActionUrl(),
                n.isRead(),
                n.getCreatedAt(),
                n.getReadAt()
        );
    }
}

