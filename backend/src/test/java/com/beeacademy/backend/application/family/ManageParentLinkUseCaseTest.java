package com.beeacademy.backend.application.family;

import com.beeacademy.backend.application.family.dto.AcceptLinkCommand;
import com.beeacademy.backend.application.family.dto.InviteStudentCommand;
import com.beeacademy.backend.application.family.dto.ParentLinkResult;
import com.beeacademy.backend.application.family.dto.UnlinkCommand;
import com.beeacademy.backend.application.family.usecase.ManageParentLinkUseCase;
import com.beeacademy.backend.domain.family.ParentLinkStatus;
import com.beeacademy.backend.domain.family.ParentStudentLink;
import com.beeacademy.backend.domain.family.ParentStudentLinkRepositoryPort;
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
class ManageParentLinkUseCaseTest {

    @Mock
    private ParentStudentLinkRepositoryPort linkRepository;

    private ManageParentLinkUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ManageParentLinkUseCase(linkRepository);
    }

    @Test
    @DisplayName("Phụ huynh gửi lời mời liên kết cho con với mã 6 ký tự")
    void inviteStudent_Success() {
        UUID parentId = UUID.randomUUID();
        InviteStudentCommand command = new InviteStudentCommand(parentId, "student@bee.edu.vn");

        when(linkRepository.save(any(ParentStudentLink.class))).thenAnswer(i -> i.getArgument(0));

        ParentLinkResult result = useCase.inviteStudent(command);

        assertThat(result).isNotNull();
        assertThat(result.studentEmail()).isEqualTo("student@bee.edu.vn");
        assertThat(result.inviteToken()).hasSize(6);
        assertThat(result.status()).isEqualTo("PENDING");
        assertThat(result.consentGranted()).isFalse();

        verify(linkRepository).save(any(ParentStudentLink.class));
    }

    @Test
    @DisplayName("Học sinh chấp nhận lời mời liên kết: chuyển ACTIVE và cấp consent xem dữ liệu")
    void acceptInvite_Success() {
        UUID parentId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        String token = "ABC123";

        ParentStudentLink link = ParentStudentLink.invite(parentId, "student@bee.edu.vn", token, 72);

        when(linkRepository.findByInviteToken(token)).thenReturn(Optional.of(link));
        when(linkRepository.save(any(ParentStudentLink.class))).thenAnswer(i -> i.getArgument(0));

        ParentLinkResult result = useCase.acceptInvite(new AcceptLinkCommand(studentId, token));

        assertThat(result.status()).isEqualTo("ACTIVE");
        assertThat(result.consentGranted()).isTrue();
        assertThat(result.studentId()).isEqualTo(studentId);
        assertThat(link.getStatus()).isEqualTo(ParentLinkStatus.ACTIVE);
    }

    @Test
    @DisplayName("Hủy liên kết: chuyển REVOKED và thu hồi quyền xem dữ liệu ngay lập tức")
    void unlink_Success() {
        UUID parentId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        ParentStudentLink link = ParentStudentLink.invite(parentId, "student@bee.edu.vn", "XYZ789", 72);
        link.accept(studentId, null);

        when(linkRepository.findById(link.getId())).thenReturn(Optional.of(link));
        when(linkRepository.save(any(ParentStudentLink.class))).thenAnswer(i -> i.getArgument(0));

        ParentLinkResult result = useCase.unlink(new UnlinkCommand(parentId, link.getId(), "Phụ huynh tự hủy"));

        assertThat(result.status()).isEqualTo("REVOKED");
        assertThat(result.consentGranted()).isFalse();
        assertThat(link.getStatus()).isEqualTo(ParentLinkStatus.REVOKED);
    }
}

