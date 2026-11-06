package com.beeacademy.backend.domain.commerce;

import java.util.Optional;
import java.util.UUID;

public interface StudentRewardBalanceRepositoryPort {

    StudentRewardBalance save(StudentRewardBalance balance);

    Optional<StudentRewardBalance> findByStudentId(UUID studentId);
}

