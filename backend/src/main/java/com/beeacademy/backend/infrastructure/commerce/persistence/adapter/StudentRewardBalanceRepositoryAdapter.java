package com.beeacademy.backend.infrastructure.commerce.persistence.adapter;

import com.beeacademy.backend.domain.commerce.StudentRewardBalance;
import com.beeacademy.backend.domain.commerce.StudentRewardBalanceRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class StudentRewardBalanceRepositoryAdapter implements StudentRewardBalanceRepositoryPort {

    private final Map<UUID, StudentRewardBalance> store = new ConcurrentHashMap<>();

    @Override
    public StudentRewardBalance save(StudentRewardBalance balance) {
        store.put(balance.getStudentId(), balance);
        return balance;
    }

    @Override
    public Optional<StudentRewardBalance> findByStudentId(UUID studentId) {
        return Optional.ofNullable(store.get(studentId));
    }
}

