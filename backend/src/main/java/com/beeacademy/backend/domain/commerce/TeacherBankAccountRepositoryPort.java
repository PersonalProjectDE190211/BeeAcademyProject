package com.beeacademy.backend.domain.commerce;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeacherBankAccountRepositoryPort {

    TeacherBankAccount save(TeacherBankAccount account);

    Optional<TeacherBankAccount> findById(UUID id);

    Optional<TeacherBankAccount> findByTeacherId(UUID teacherId);

    List<TeacherBankAccount> findByVerifyStatus(BankVerifyStatus status);
}

