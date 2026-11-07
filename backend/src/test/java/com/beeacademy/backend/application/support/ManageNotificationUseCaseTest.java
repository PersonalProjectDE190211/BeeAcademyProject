package com.beeacademy.backend.application.support;

import com.beeacademy.backend.application.support.dto.NotificationResult;
import com.beeacademy.backend.application.support.dto.SendNotificationCommand;
import com.beeacademy.backend.application.support.usecase.ManageNotificationUseCase;
import com.beeacademy.backend.domain.support.NotificationRepositoryPort;
import com.beeacademy.backend.domain.support.NotificationType;
import com.beeacademy.backend.domain.support.UserNotification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManageNotificationUseCaseTest {

    @Mock
    private NotificationRepositoryPort notificationRepository;

    private ManageNotificationUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ManageNotificationUseCase(notificationRepository);
    }

    @Test
    @DisplayName("Gửi thông báo mới cho người dùng thành công")
    void sendNotification_Success() {
        UUID recipientId = UUID.randomUUID();
        SendNotificationCommand command = new SendNotificationCommand(
                recipientId,
                "Chúc mừng bạn đạt chứng chỉ!",
                "Khóa học Toán 10 của bạn đã hoàn thành 100%",
                NotificationType.COURSE_UPDATE,
                "/certificates/123"
        );

        when(notificationRepository.saveUserNotification(any(UserNotification.class))).thenAnswer(i -> i.getArgument(0));

        NotificationResult result = useCase.sendNotification(command);

        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("Chúc mừng bạn đạt chứng chỉ!");
        assertThat(result.type()).isEqualTo("COURSE_UPDATE");
        assertThat(result.read()).isFalse();

        verify(notificationRepository).saveUserNotification(any(UserNotification.class));
    }

    @Test
    @DisplayName("Đánh dấu thông báo đã đọc thành công")
    void markAsRead_Success() {
        UUID recipientId = UUID.randomUUID();
        UserNotification notification = UserNotification.dispatch(
                recipientId, "Nhắc nhở học tập", "Đã 3 ngày bạn chưa vào học", NotificationType.GENERAL, null
        );

        when(notificationRepository.findUserNotificationById(notification.getId())).thenReturn(Optional.of(notification));
        when(notificationRepository.saveUserNotification(any(UserNotification.class))).thenAnswer(i -> i.getArgument(0));

        NotificationResult result = useCase.markAsRead(notification.getId(), recipientId);

        assertThat(result.read()).isTrue();
        assertThat(result.readAt()).isNotNull();
    }
}

