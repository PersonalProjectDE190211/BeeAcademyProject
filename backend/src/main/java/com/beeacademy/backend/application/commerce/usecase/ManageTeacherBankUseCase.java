package com.beeacademy.backend.application.commerce.usecase;

import com.beeacademy.backend.application.commerce.dto.TeacherBankAccountCommand;
import com.beeacademy.backend.application.commerce.dto.TeacherBankAccountResult;
import com.beeacademy.backend.domain.commerce.BankVerifyStatus;
import com.beeacademy.backend.domain.commerce.TeacherBankAccount;
import com.beeacademy.backend.domain.commerce.TeacherBankAccountRepositoryPort;
import com.beeacademy.backend.domain.common.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ManageTeacherBankUseCase {

    private final TeacherBankAccountRepositoryPort bankAccountRepository;

    public ManageTeacherBankUseCase(TeacherBankAccountRepositoryPort bankAccountRepository) {
        this.bankAccountRepository = bankAccountRepository;
    }

    @Transactional(readOnly = true)
    public Optional<TeacherBankAccountResult> getBankAccount(UUID teacherId) {
        return bankAccountRepository.findByTeacherId(teacherId)
                .map(this::toResult);
    }

    @Transactional
    public TeacherBankAccountResult saveOrUpdateBankAccount(UUID teacherId, TeacherBankAccountCommand command) {
        Optional<TeacherBankAccount> existingOpt = bankAccountRepository.findByTeacherId(teacherId);

        TeacherBankAccount account;
        if (existingOpt.isPresent()) {
            account = existingOpt.get();
            account.updateAccountDetails(
                    command.bankName(),
                    command.accountNumber(),
                    command.accountHolder(),
                    command.branch(),
                    "TEACHER",
                    "Giáo viên cập nhật thông tin tài khoản ngân hàng"
            );
        } else {
            account = TeacherBankAccount.create(
                    teacherId,
                    command.bankName(),
                    command.accountNumber(),
                    command.accountHolder(),
                    command.branch()
            );
        }

        TeacherBankAccount saved = bankAccountRepository.save(account);
        return toResult(saved);
    }

    @Transactional
    public TeacherBankAccountResult verifyBankAccount(UUID bankAccountId, UUID adminId) {
        TeacherBankAccount account = bankAccountRepository.findById(bankAccountId)
                .orElseThrow(() -> new DomainException("BANK_ACCOUNT_NOT_FOUND", "Không tìm thấy thông tin tài khoản ngân hàng"));

        account.verifyByAdmin(adminId);
        TeacherBankAccount saved = bankAccountRepository.save(account);
        return toResult(saved);
    }

    @Transactional
    public TeacherBankAccountResult rejectBankAccount(UUID bankAccountId, UUID adminId, String reason) {
        TeacherBankAccount account = bankAccountRepository.findById(bankAccountId)
                .orElseThrow(() -> new DomainException("BANK_ACCOUNT_NOT_FOUND", "Không tìm thấy thông tin tài khoản ngân hàng"));

        account.rejectByAdmin(adminId, reason);
        TeacherBankAccount saved = bankAccountRepository.save(account);
        return toResult(saved);
    }

    @Transactional(readOnly = true)
    public List<TeacherBankAccountResult> getPendingBankAccounts() {
        return bankAccountRepository.findByVerifyStatus(BankVerifyStatus.PENDING).stream()
                .map(this::toResult)
                .collect(Collectors.toList());
    }

    private TeacherBankAccountResult toResult(TeacherBankAccount account) {
        String raw = account.getAccountNumber();
        String masked = raw;
        if (raw != null && raw.length() > 4) {
            masked = "*".repeat(raw.length() - 4) + raw.substring(raw.length() - 4);
        }

        return new TeacherBankAccountResult(
                account.getId(),
                account.getTeacherId(),
                account.getBankName(),
                masked,
                account.getAccountHolder(),
                account.getBranch(),
                account.getVerifyStatus().name(),
                account.isOtpVerified(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}

