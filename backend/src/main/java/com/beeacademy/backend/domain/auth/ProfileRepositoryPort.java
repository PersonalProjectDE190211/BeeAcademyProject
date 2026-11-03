package com.beeacademy.backend.domain.auth;

import java.util.Optional;
import java.util.UUID;

/**
 * Domain Repository Port cho Aggregate Root {@link Profile}.
 * Định nghĩa các thao tác lưu trữ và truy vấn nghiệp vụ theo nguyên lý Dependency Inversion.
 * Tầng Infrastructure sẽ thực thi (implement) port này bằng Spring Data JPA.
 */
public interface ProfileRepositoryPort {

    Optional<Profile> findById(UUID id);

    Optional<Profile> findByEmail(String email);

    boolean existsByEmail(String email);

    Profile save(Profile profile);
}

