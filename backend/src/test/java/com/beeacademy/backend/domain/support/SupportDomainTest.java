package com.beeacademy.backend.domain.support;

import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.support.event.ComplaintCreatedDomainEvent;
import com.beeacademy.backend.domain.support.event.ComplaintResolvedDomainEvent;
import com.beeacademy.backend.domain.support.event.NotificationDispatchedDomainEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SupportDomainTest {

    @Nested
    @DisplayName("1. Kiểm thử Khiếu nại người dùng (Complaint Aggregate - UC12, UC42)")
    class ComplaintTests {

        @Test
        @DisplayName("Mở khiếu nại thành công với trạng thái OPEN và tin nhắn khởi tạo")
        void testOpenComplaintSuccess() {
            UUID senderId = UUID.randomUUID();
            String title = "Lỗi không xem được video bài 3";
            String initialMessage = "Video bài 3 báo lỗi 403 không tải được";

            Complaint complaint = Complaint.open(
                    senderId, "STUDENT", title, ComplaintCategory.TECHNICAL, ComplaintPriority.HIGH, initialMessage
            );

            assertThat(complaint.getStatus()).isEqualTo(ComplaintStatus.OPEN);
            assertThat(complaint.getSenderId()).isEqualTo(senderId);
            assertThat(complaint.getCategory()).isEqualTo(ComplaintCategory.TECHNICAL);
            assertThat(complaint.getPriority()).isEqualTo(ComplaintPriority.HIGH);
            assertThat(complaint.getMessages()).hasSize(1);
            assertThat(complaint.getMessages().getFirst().getContent()).isEqualTo(initialMessage);
            assertThat(complaint.getDomainEvents()).hasSize(1);
            assertThat(complaint.getDomainEvents().getFirst()).isInstanceOf(ComplaintCreatedDomainEvent.class);
        }

        @Test
        @DisplayName("Admin phản hồi khiếu nại chuyển trạng thái sang IN_PROGRESS")
        void testAdminReplyTransitionsToInProgress() {
            UUID senderId = UUID.randomUUID();
            UUID adminId = UUID.randomUUID();
            Complaint complaint = Complaint.open(
                    senderId, "STUDENT", "Khiếu nại thanh toán", ComplaintCategory.PAYMENT, ComplaintPriority.URGENT, "Đã trừ tiền nhưng chưa kích hoạt"
            );

            // Đính kèm ảnh biên lai
            complaint.addAttachment("https://cdn.beeacademy.com/proof/receipt.png", "receipt.png", 1024 * 250);
            assertThat(complaint.getAttachments()).hasSize(1);

            // Admin gửi tin nhắn phản hồi
            complaint.addMessage(adminId, "ADMIN", "Ban quản trị đã nhận được và đang đối soát với PayOS");

            assertThat(complaint.getStatus()).isEqualTo(ComplaintStatus.IN_PROGRESS);
            assertThat(complaint.getMessages()).hasSize(2);
        }

        @Test
        @DisplayName("Admin giải quyết khiếu nại (RESOLVE) và phát sinh ComplaintResolvedDomainEvent")
        void testResolveComplaintSuccess() {
            UUID senderId = UUID.randomUUID();
            UUID adminId = UUID.randomUUID();
            Complaint complaint = Complaint.open(
                    senderId, "PARENT", "Học sinh không thể làm bài", ComplaintCategory.TECHNICAL, ComplaintPriority.MEDIUM, "Không ấn nộp bài được"
            );
            complaint.clearDomainEvents();

            complaint.resolve(adminId, "Đã sửa xong lỗi bộ đệm và hoàn tiền bài thi");

            assertThat(complaint.getStatus()).isEqualTo(ComplaintStatus.RESOLVED);
            assertThat(complaint.getResolvedByAdminId()).isEqualTo(adminId);
            assertThat(complaint.getResolutionSummary()).contains("Đã sửa xong lỗi");
            assertThat(complaint.getResolvedAt()).isNotNull();
            assertThat(complaint.getDomainEvents()).hasSize(1);
            assertThat(complaint.getDomainEvents().getFirst()).isInstanceOf(ComplaintResolvedDomainEvent.class);
        }

        @Test
        @DisplayName("Người dùng phản hồi lại khiếu nại đã RESOLVED sẽ tự động mở lại thành IN_PROGRESS")
        void testUserReplyReopensResolvedComplaint() {
            UUID senderId = UUID.randomUUID();
            UUID adminId = UUID.randomUUID();
            Complaint complaint = Complaint.open(
                    senderId, "STUDENT", "Thắc mắc nội dung bài giảng", ComplaintCategory.COURSE_CONTENT, ComplaintPriority.LOW, "Bài 4 có chỗ khó hiểu"
            );
            complaint.resolve(adminId, "Đã liên hệ giáo viên giải đáp");
            assertThat(complaint.getStatus()).isEqualTo(ComplaintStatus.RESOLVED);

            // Học sinh tiếp tục thắc mắc
            complaint.addMessage(senderId, "STUDENT", "Em vẫn chưa nhận được email của giáo viên ạ");

            assertThat(complaint.getStatus()).isEqualTo(ComplaintStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("Không thể gửi tin nhắn khi khiếu nại đã đóng (CLOSED)")
        void testCannotMessageOnClosedComplaint() {
            UUID senderId = UUID.randomUUID();
            Complaint complaint = Complaint.open(
                    senderId, "TEACHER", "Góp ý giao diện", ComplaintCategory.OTHER, ComplaintPriority.LOW, "Giao diện mới rất đẹp"
            );
            complaint.close();

            assertThatThrownBy(() -> complaint.addMessage(senderId, "TEACHER", "Gửi thêm lời khen"))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("Khiếu nại đã đóng");
        }
    }

    @Nested
    @DisplayName("2. Kiểm thử Thông báo (AdminNotification & UserNotification - UC44)")
    class NotificationTests {

        @Test
        @DisplayName("Gửi thông báo đến người dùng và đánh dấu đã đọc")
        void testUserNotificationWorkflow() {
            UUID recipientId = UUID.randomUUID();

            UserNotification notification = UserNotification.dispatch(
                    recipientId,
                    "Kỳ thi đã chấm xong!",
                    "Bạn đạt 9.5 điểm bài thi Toán Giữa Kỳ",
                    NotificationType.EXAM_RESULT,
                    "/exams/result/123"
            );

            assertThat(notification.isRead()).isFalse();
            assertThat(notification.getRecipientId()).isEqualTo(recipientId);
            assertThat(notification.getDomainEvents()).hasSize(1);
            assertThat(notification.getDomainEvents().getFirst()).isInstanceOf(NotificationDispatchedDomainEvent.class);

            // Đánh dấu đã đọc
            notification.markAsRead();
            assertThat(notification.isRead()).isTrue();
            assertThat(notification.getReadAt()).isNotNull();

            // Đánh dấu chưa đọc
            notification.markAsUnread();
            assertThat(notification.isRead()).isFalse();
            assertThat(notification.getReadAt()).isNull();
        }

        @Test
        @DisplayName("Tạo và quản lý thông báo Admin")
        void testAdminNotificationWorkflow() {
            AdminNotification adminNotif = AdminNotification.create(
                    "Cảnh báo gian lận thi cử",
                    "Học sinh đã chuyển tab 4 lần trong kỳ thi",
                    NotificationType.SYSTEM_ALERT,
                    "/admin/exams/integrity/456"
            );

            assertThat(adminNotif.isRead()).isFalse();
            assertThat(adminNotif.getTitle()).isEqualTo("Cảnh báo gian lận thi cử");

            adminNotif.markAsRead();
            assertThat(adminNotif.isRead()).isTrue();
        }
    }
}
