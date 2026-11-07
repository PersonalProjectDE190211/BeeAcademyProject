package com.beeacademy.backend.application.commerce;

import com.beeacademy.backend.application.commerce.dto.TeacherBankAccountCommand;
import com.beeacademy.backend.application.commerce.dto.TeacherBankAccountResult;
import com.beeacademy.backend.application.commerce.usecase.ManageTeacherBankUseCase;
import com.beeacademy.backend.domain.commerce.BankVerifyStatus;
import com.beeacademy.backend.domain.commerce.TeacherBankAccount;
import com.beeacademy.backend.domain.commerce.TeacherBankAccountRepositoryPort;
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
class ManageTeacherBankUseCaseTest {

    @Mock
    private TeacherBankAccountRepositoryPort bankAccountRepository;

    private ManageTeacherBankUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ManageTeacherBankUseCase(bankAccountRepository);
    }

    @Test
    @DisplayName("Tạo mới tài khoản ngân hàng cho giáo viên với mặt nạ số tài khoản")
    void createBankAccount_Success() {
        UUID teacherId = UUID.randomUUID();
        TeacherBankAccountCommand command = new TeacherBankAccountCommand(
                "Vietcombank",
                "0123456789",
                "NGUYEN VAN A",
                "Chi nhánh Hà Nội"
        );

        when(bankAccountRepository.findByTeacherId(teacherId)).thenReturn(Optional.empty());
        when(bankAccountRepository.save(any(TeacherBankAccount.class))).thenAnswer(i -> i.getArgument(0));

        TeacherBankAccountResult result = useCase.saveOrUpdateBankAccount(teacherId, command);

        assertThat(result).isNotNull();
        assertThat(result.bankName()).isEqualTo("Vietcombank");
        assertThat(result.accountHolder()).isEqualTo("NGUYEN VAN A");
        assertThat(result.verifyStatus()).isEqualTo("PENDING");
        assertThat(result.accountNumberMasked()).isEqualTo("******6789");

        verify(bankAccountRepository).save(any(TeacherBankAccount.class));
    }

    @Test
    @DisplayName("Admin duyệt xác thực tài khoản ngân hàng của giáo viên")
    void verifyBankAccount_Success() {
        UUID bankAccountId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        TeacherBankAccount account = TeacherBankAccount.create(
                UUID.randomUUID(),
                "MBBank",
                "9876543210",
                "TRAN THI B",
                "Hồ Chí Minh"
        );

        when(bankAccountRepository.findById(bankAccountId)).thenReturn(Optional.of(account));
        when(bankAccountRepository.save(any(TeacherBankAccount.class))).thenAnswer(i -> i.getArgument(0));

        TeacherBankAccountResult result = useCase.verifyBankAccount(bankAccountId, adminId);

        assertThat(result.verifyStatus()).isEqualTo("VERIFIED");
        assertThat(account.getVerifyStatus()).isEqualTo(BankVerifyStatus.VERIFIED);
    }
}

