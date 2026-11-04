package com.beeacademy.backend.domain.support;

import com.beeacademy.backend.domain.common.AbstractAggregateRoot;
import com.beeacademy.backend.domain.common.DomainException;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root đại diện cho Thông báo gửi đến Ban quản trị / Admin.
 * (Ví dụ: Yêu cầu rút tiền mới, khiếu nại nghiêm trọng, cảnh báo gian lận thi cử).
 */
public class AdminNotification extends AbstractAggregateRoot {

    private final UUID id;
    private final String title;
    private final String message;
    private final NotificationType type;
    private boolean read;
    private final String actionUrl;
    private final Instant createdAt;
    private Instant readAt;

    public AdminNotification(
            UUID id,
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
        if (title == null || title.isBlank()) {
            throw new DomainException("INVALID_NOTIFICATION", "Tiêu đề thông báo không được để trống");
        }
        this.id = id;
        this.title = title.trim();
        this.message = message == null ? "" : message.trim();
        this.type = type == null ? NotificationType.SYSTEM_ALERT : type;
        this.read = read;
        this.actionUrl = actionUrl;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.readAt = readAt;
    }

    public static AdminNotification create(String title, String message, NotificationType type, String actionUrl) {
        return new AdminNotification(UUID.randomUUID(), title, message, type, false, actionUrl, Instant.now(), null);
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
        AdminNotification that = (AdminNotification) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
