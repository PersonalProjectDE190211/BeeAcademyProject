package com.beeacademy.backend.domain.family;

import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.family.event.ParentLinkAcceptedDomainEvent;
import com.beeacademy.backend.domain.family.event.ParentLinkInvitedDomainEvent;
import com.beeacademy.backend.domain.family.event.ParentLinkRevokedDomainEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FamilyDomainTest {

    @Nested
    @DisplayName("1. Vòng đời liên kết Phụ huynh - Học sinh (UC27, UC28, UC29)")
    class LinkLifecycleTests {

        @Test
        @DisplayName("Phụ huynh gửi lời mời tạo liên kết PENDING và phát sinh ParentLinkInvitedDomainEvent")
        void testInviteLinkSuccess() {
            UUID parentId = UUID.randomUUID();
            String studentEmail = "hocsinh@beeacademy.com";
            String token = "INVITE-SECURE-TOKEN-123";

            ParentStudentLink link = ParentStudentLink.invite(parentId, studentEmail, token, 72);

            assertThat(link.getStatus()).isEqualTo(ParentLinkStatus.PENDING);
            assertThat(link.getParentId()).isEqualTo(parentId);
            assertThat(link.getStudentEmail()).isEqualTo("hocsinh@beeacademy.com");
            assertThat(link.isSensitiveDataConsentGranted()).isFalse();
            assertThat(link.getInviteExpiresAt()).isAfter(Instant.now());
            assertThat(link.getDomainEvents()).hasSize(1);
            assertThat(link.getDomainEvents().getFirst()).isInstanceOf(ParentLinkInvitedDomainEvent.class);
            assertThat(link.getAuditLogs()).hasSize(1);
        }

        @Test
        @DisplayName("Học sinh chấp thuận kích hoạt liên kết ACTIVE và cấp quyền xem dữ liệu")
        void testAcceptLinkSuccess() {
            UUID parentId = UUID.randomUUID();
            UUID studentId = UUID.randomUUID();
            ParentStudentLink link = ParentStudentLink.invite(parentId, "hocsinh@beeacademy.com", "TOKEN-1", 72);
            link.clearDomainEvents();

            link.accept(studentId, Instant.now());

            assertThat(link.getStatus()).isEqualTo(ParentLinkStatus.ACTIVE);
            assertThat(link.getStudentId()).isEqualTo(studentId);
            assertThat(link.isSensitiveDataConsentGranted()).isTrue();
            assertThat(link.isDataAccessibleByParent(parentId)).isTrue();
            assertThat(link.getDomainEvents()).hasSize(1);
            assertThat(link.getDomainEvents().getFirst()).isInstanceOf(ParentLinkAcceptedDomainEvent.class);
            assertThat(link.getAuditLogs()).hasSize(2); // INVITED, ACCEPTED
        }

        @Test
        @DisplayName("Lời mời quá hạn 72 giờ sẽ bị từ chối khi học sinh chấp thuận")
        void testAcceptExpiredInviteThrows() {
            UUID parentId = UUID.randomUUID();
            Instant pastTime = Instant.now().minus(80, ChronoUnit.HOURS);
            ParentStudentLink link = new ParentStudentLink(
                    UUID.randomUUID(),
                    parentId,
                    null,
                    "student@bee.com",
                    "EXPIRED-TOKEN",
                    ParentLinkStatus.PENDING,
                    false,
                    pastTime.plus(72, ChronoUnit.HOURS), // Đã hết hạn 8 giờ trước
                    null,
                    null,
                    null,
                    null,
                    pastTime,
                    pastTime,
                    null
            );

            assertThatThrownBy(() -> link.accept(UUID.randomUUID(), Instant.now()))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("hết hạn");
            assertThat(link.getStatus()).isEqualTo(ParentLinkStatus.REJECTED);
        }

        @Test
        @DisplayName("Cả Phụ huynh hoặc Học sinh đều có quyền hủy liên kết (UC29)")
        void testBothParentAndStudentCanUnlink() {
            UUID parentId = UUID.randomUUID();
            UUID studentId = UUID.randomUUID();
            ParentStudentLink link = ParentStudentLink.invite(parentId, "student@bee.com", "TOKEN-2", 72);
            link.accept(studentId, Instant.now());
            link.clearDomainEvents();

            // Học sinh thực hiện hủy liên kết
            link.unlink(studentId, "Con muốn tự quản lý việc học");

            assertThat(link.getStatus()).isEqualTo(ParentLinkStatus.REVOKED);
            assertThat(link.isSensitiveDataConsentGranted()).isFalse();
            assertThat(link.getUnlinkedBy()).isEqualTo(studentId);
            assertThat(link.getUnlinkReason()).isEqualTo("Con muốn tự quản lý việc học");
            assertThat(link.isDataAccessibleByParent(parentId)).isFalse();
            assertThat(link.getDomainEvents()).hasSize(1);
            assertThat(link.getDomainEvents().getFirst()).isInstanceOf(ParentLinkRevokedDomainEvent.class);
        }

        @Test
        @DisplayName("Người lạ (không phải Phụ huynh hay Học sinh) không được phép hủy liên kết")
        void testStrangerCannotUnlink() {
            UUID parentId = UUID.randomUUID();
            UUID studentId = UUID.randomUUID();
            UUID strangerId = UUID.randomUUID();
            ParentStudentLink link = ParentStudentLink.invite(parentId, "student@bee.com", "TOKEN-3", 72);
            link.accept(studentId, Instant.now());

            assertThatThrownBy(() -> link.unlink(strangerId, "Kẻ xấu muốn can thiệp"))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("Chỉ Phụ huynh hoặc Học sinh trong mối liên kết");
        }
    }

    @Nested
    @DisplayName("2. Kiểm toán truy cập dữ liệu nhạy cảm (UC24 Data Access Auditing)")
    class PrivacyAuditTests {

        @Test
        @DisplayName("Ghi vết phụ huynh truy cập điểm và bảng điểm học sinh")
        void testParentProgressAccessAudit() {
            UUID parentId = UUID.randomUUID();
            UUID studentId = UUID.randomUUID();

            ParentProgressAccessAudit audit = ParentProgressAccessAudit.record(
                    parentId, studentId, "VIEW_EXAM_SCORE"
            );

            assertThat(audit.getId()).isNotNull();
            assertThat(audit.getParentId()).isEqualTo(parentId);
            assertThat(audit.getStudentId()).isEqualTo(studentId);
            assertThat(audit.getAccessType()).isEqualTo("VIEW_EXAM_SCORE");
            assertThat(audit.getAccessedAt()).isNotNull();
        }
    }
}
