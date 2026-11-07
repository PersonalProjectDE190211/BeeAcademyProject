package com.beeacademy.backend.infrastructure.commerce.persistence.adapter;

import com.beeacademy.backend.domain.commerce.BankVerifyStatus;
import com.beeacademy.backend.domain.commerce.TeacherBankAccount;
import com.beeacademy.backend.domain.commerce.TeacherBankAccountRepositoryPort;
import com.beeacademy.backend.domain.commerce.TeacherBankAuditLog;
import com.beeacademy.backend.infrastructure.commerce.persistence.entity.TeacherBankAccountJpaEntity;
import com.beeacademy.backend.infrastructure.commerce.persistence.entity.TeacherBankAuditLogJpaEntity;
import com.beeacademy.backend.infrastructure.commerce.persistence.repository.SpringDataTeacherBankAuditLogRepository;
import com.beeacademy.backend.infrastructure.commerce.persistence.repository.SpringDataTeacherBankAccountRepository;
import com.beeacademy.backend.infrastructure.common.adapter.AesEncryptionAdapter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class TeacherBankAccountRepositoryAdapter implements TeacherBankAccountRepositoryPort {

    private final SpringDataTeacherBankAccountRepository bankAccountRepository;
    private final SpringDataTeacherBankAuditLogRepository auditLogRepository;
    private final AesEncryptionAdapter aesEncryptionAdapter;

    public TeacherBankAccountRepositoryAdapter(
            SpringDataTeacherBankAccountRepository bankAccountRepository,
            SpringDataTeacherBankAuditLogRepository auditLogRepository,
            AesEncryptionAdapter aesEncryptionAdapter
    ) {
        this.bankAccountRepository = bankAccountRepository;
        this.auditLogRepository = auditLogRepository;
        this.aesEncryptionAdapter = aesEncryptionAdapter;
    }

    @Override
    @Transactional
    public TeacherBankAccount save(TeacherBankAccount account) {
        String encrypted = aesEncryptionAdapter.encrypt(account.getAccountNumber());
        String masked = aesEncryptionAdapter.maskAccountNumber(account.getAccountNumber());

        TeacherBankAccountJpaEntity entity = new TeacherBankAccountJpaEntity(
                account.getId(),
                account.getTeacherId(),
                account.getBankName(),
                encrypted,
                masked,
                account.getAccountHolder(),
                account.getBranch(),
                account.getVerifyStatus().name().toLowerCase(),
                account.isOtpVerified(),
                null,
                null,
                account.getCreatedAt(),
                account.getUpdatedAt()
        );

        TeacherBankAccountJpaEntity saved = bankAccountRepository.save(entity);

        // Lưu audit logs nếu có
        for (TeacherBankAuditLog log : account.getAuditLogs()) {
            if (!auditLogRepository.existsById(log.getId())) {
                TeacherBankAuditLogJpaEntity logEntity = new TeacherBankAuditLogJpaEntity(
                        log.getId(),
                        log.getTeacherId(),
                        log.getChangedAt(),
                        log.getChangedByName(),
                        log.getReason(),
                        log.getChangesJson()
                );
                auditLogRepository.save(logEntity);
            }
        }

        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TeacherBankAccount> findById(UUID id) {
        return bankAccountRepository.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TeacherBankAccount> findByTeacherId(UUID teacherId) {
        return bankAccountRepository.findByTeacherId(teacherId).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeacherBankAccount> findByVerifyStatus(BankVerifyStatus status) {
        return bankAccountRepository.findByVerifyStatus(status.name().toLowerCase()).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private TeacherBankAccount toDomain(TeacherBankAccountJpaEntity entity) {
        String plainAccountNumber = aesEncryptionAdapter.decrypt(entity.getAccountNumberEncrypted());
        BankVerifyStatus status = BankVerifyStatus.valueOf(entity.getVerifyStatus().toUpperCase());

        List<TeacherBankAuditLog> logs = new ArrayList<>();
        try {
            logs = auditLogRepository.findByTeacherIdOrderByChangedAtDesc(entity.getTeacherId()).stream()
                    .map(l -> new TeacherBankAuditLog(
                            l.getId(),
                            l.getTeacherId(),
                            l.getChangedAt(),
                            l.getChangedByName(),
                            l.getReason(),
                            l.getChanges()
                    ))
                    .collect(Collectors.toList());
        } catch (Exception ignored) {
            // Trường hợp bảng audit log chưa có dữ liệu
        }

        return new TeacherBankAccount(
                entity.getId(),
                entity.getTeacherId(),
                entity.getBankName(),
                plainAccountNumber,
                entity.getAccountHolder(),
                entity.getBranch(),
                status,
                entity.isOtpVerified(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                logs
        );
    }
}

