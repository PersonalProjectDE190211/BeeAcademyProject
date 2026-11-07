package com.beeacademy.backend.application.support;

import com.beeacademy.backend.application.support.dto.AddComplaintMessageCommand;
import com.beeacademy.backend.application.support.dto.ComplaintResult;
import com.beeacademy.backend.application.support.dto.CreateComplaintCommand;
import com.beeacademy.backend.application.support.dto.ResolveComplaintCommand;
import com.beeacademy.backend.application.support.usecase.ManageComplaintUseCase;
import com.beeacademy.backend.domain.support.Complaint;
import com.beeacademy.backend.domain.support.ComplaintCategory;
import com.beeacademy.backend.domain.support.ComplaintPriority;
import com.beeacademy.backend.domain.support.ComplaintRepositoryPort;
import com.beeacademy.backend.domain.support.ComplaintStatus;
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
class ManageComplaintUseCaseTest {

    @Mock
    private ComplaintRepositoryPort complaintRepository;

    private ManageComplaintUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ManageComplaintUseCase(complaintRepository);
    }

    @Test
    @DisplayName("Tạo khiếu nại mới thành công: trạng thái OPEN và có 1 tin nhắn khởi tạo")
    void createComplaint_Success() {
        UUID senderId = UUID.randomUUID();
        CreateComplaintCommand command = new CreateComplaintCommand(
                senderId,
                "STUDENT",
                "Video bài giảng bị lỗi âm thanh",
                ComplaintCategory.COURSE_CONTENT,
                ComplaintPriority.HIGH,
                "Bài số 3 chương 2 không nghe thấy tiếng giảng"
        );

        when(complaintRepository.save(any(Complaint.class))).thenAnswer(i -> i.getArgument(0));

        ComplaintResult result = useCase.createComplaint(command);

        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("Video bài giảng bị lỗi âm thanh");
        assertThat(result.status()).isEqualTo("OPEN");
        assertThat(result.priority()).isEqualTo("HIGH");
        assertThat(result.messages()).hasSize(1);
        assertThat(result.messages().get(0).content()).contains("không nghe thấy tiếng");

        verify(complaintRepository).save(any(Complaint.class));
    }

    @Test
    @DisplayName("Admin phản hồi khiếu nại: tự động chuyển trạng thái từ OPEN sang IN_PROGRESS")
    void addMessage_AdminReply_ChangesStatusToInProgress() {
        UUID senderId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        Complaint complaint = Complaint.open(
                senderId, "STUDENT", "Lỗi thanh toán", ComplaintCategory.PAYMENT, ComplaintPriority.URGENT, "Đã trừ tiền nhưng chưa kích hoạt"
        );

        when(complaintRepository.findById(complaint.getId())).thenReturn(Optional.of(complaint));
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(i -> i.getArgument(0));

        AddComplaintMessageCommand command = new AddComplaintMessageCommand(
                complaint.getId(), adminId, "ADMIN", "Ban quản trị đã kiểm tra và đối soát đơn hàng"
        );

        ComplaintResult result = useCase.addMessage(command);

        assertThat(result.status()).isEqualTo("IN_PROGRESS");
        assertThat(result.messages()).hasSize(2);
        assertThat(complaint.getStatus()).isEqualTo(ComplaintStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("Admin giải quyết khiếu nại: chuyển sang RESOLVED kèm kết luận")
    void resolveComplaint_Success() {
        UUID adminId = UUID.randomUUID();
        Complaint complaint = Complaint.open(
                UUID.randomUUID(), "STUDENT", "Góp ý giao diện", ComplaintCategory.OTHER, ComplaintPriority.LOW, "Nút nộp bài hơi nhỏ"
        );

        when(complaintRepository.findById(complaint.getId())).thenReturn(Optional.of(complaint));
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(i -> i.getArgument(0));

        ResolveComplaintCommand command = new ResolveComplaintCommand(
                complaint.getId(), adminId, "Đã tối ưu kích thước nút bấm trên phiên bản mới"
        );

        ComplaintResult result = useCase.resolveComplaint(command);

        assertThat(result.status()).isEqualTo("RESOLVED");
        assertThat(result.resolvedByAdminId()).isEqualTo(adminId);
        assertThat(result.resolutionSummary()).contains("Đã tối ưu");
    }
}

