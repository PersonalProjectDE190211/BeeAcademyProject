package com.beeacademy.backend.domain.support;

import com.beeacademy.backend.domain.common.AbstractAggregateRoot;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.support.event.NotificationDispatchedDomainEvent;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root đại diện cho Thông báo gửi đến Người dùng (Học sinh, Phụ huynh, Giáo viên) (UC44).
 */
public class UserNotification extends AbstractAggregateRoot {

    private final UUID id;
    private final UUID recipientId;
    private final String title;
    private final String message;
    private final NotificationType type;
    private boolean read;
    private final String actionUrl;
    private final Instant createdAt;
    private Instant readAt;

    public UserNotification(
            UUID id,
            UUID recipientId,
            String title,
            String message,
            NotificationType type,
            boolean read,
            String actionUrl,
            Instant createdAt,
            Instant readAt
    ) {
        if (id == null) {
            throw new DomainException("INVALID_NOTIFICATION", "ID không được null");
        }
        if (recipientId == null) {
            throw new DomainException("INVALID_NOTIFICATION", "Recipient ID không được null");
        }
        if (title == null || title.isBlank()) {
            throw new DomainException("INVALID_NOTIFICATION", "Tiêu đề thông báo không được để trống");
        }
        this.id = id;
        this.recipientId = recipientId;
        this.title = title.trim();
        this.message = message == null ? "" : message.trim();
        this.type = type == null ? NotificationType.GENERAL : type;
        this.read = read;
        this.actionUrl = actionUrl;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.readAt = readAt;
    }

    public static UserNotification dispatch(
            UUID recipientId,
            String title,
            String message,
            NotificationType type,
            String actionUrl
    ) {
        UUID notifId = UUID.randomUUID();
        UserNotification notification = new UserNotification(
                notifId,
                recipientId,
                title,
                message,
                type,
                false,
                actionUrl,
                Instant.now(),
                null
        );
        notification.registerDomainEvent(new NotificationDispatchedDomainEvent(notifId, recipientId, title, type));
        return notification;
    }

    public void markAsRead() {
        this.read = true;
        this.readAt = Instant.now();
    }

    public void markAsUnread() {
        this.read = false;
        this.readAt = null;
    }

    public UUID getId() {
        return id;
    }

    public UUID getRecipientId() {
        return recipientId;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public NotificationType getType() {
        return type;
    }

    public boolean isRead() {
        return read;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getReadAt() {
        return readAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserNotification that = (UserNotification) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
