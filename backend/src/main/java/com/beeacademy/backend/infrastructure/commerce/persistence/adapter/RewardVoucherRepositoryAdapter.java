package com.beeacademy.backend.infrastructure.commerce.persistence.adapter;

import com.beeacademy.backend.domain.commerce.RewardVoucher;
import com.beeacademy.backend.domain.commerce.RewardVoucherRepositoryPort;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class RewardVoucherRepositoryAdapter implements RewardVoucherRepositoryPort {

    private final Map<UUID, RewardVoucher> storeById = new ConcurrentHashMap<>();
    private final Map<String, UUID> storeByCode = new ConcurrentHashMap<>();

    @Override
    public RewardVoucher save(RewardVoucher voucher) {
        storeById.put(voucher.getId(), voucher);
        storeByCode.put(voucher.getCode().toUpperCase(), voucher.getId());
        return voucher;
    }

    @Override
    public Optional<RewardVoucher> findById(UUID id) {
        return Optional.ofNullable(storeById.get(id));
    }

    @Override
    public Optional<RewardVoucher> findByCode(String code) {
        if (code == null) {
            return Optional.empty();
        }
        UUID id = storeByCode.get(code.trim().toUpperCase());
        return id != null ? Optional.ofNullable(storeById.get(id)) : Optional.empty();
    }

    @Override
    public List<RewardVoucher> findAvailableForStudent(UUID studentId) {
        Instant now = Instant.now();
        return storeById.values().stream()
                .filter(v -> v.isValid(now))
                .filter(v -> v.getAssignedStudentId() == null || v.getAssignedStudentId().equals(studentId))
                .collect(Collectors.toList());
    }
}

