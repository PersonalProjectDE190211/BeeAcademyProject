package com.beeacademy.backend.domain.commerce;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RewardVoucherRepositoryPort {

    RewardVoucher save(RewardVoucher voucher);

    Optional<RewardVoucher> findById(UUID id);

    Optional<RewardVoucher> findByCode(String code);

    List<RewardVoucher> findAvailableForStudent(UUID studentId);
}
