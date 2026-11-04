package com.beeacademy.backend.domain.support;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepositoryPort {

    AdminNotification saveAdminNotification(AdminNotification notification);

    Optional<AdminNotification> findAdminNotificationById(UUID id);

    List<AdminNotification> findUnreadAdminNotifications();

    List<AdminNotification> findAllAdminNotifications();

    UserNotification saveUserNotification(UserNotification notification);

    Optional<UserNotification> findUserNotificationById(UUID id);

    List<UserNotification> findByRecipientId(UUID recipientId);

    List<UserNotification> findUnreadByRecipientId(UUID recipientId);
}
