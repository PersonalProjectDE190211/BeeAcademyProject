package com.beeacademy.backend.infrastructure.auth.persistence.repository;

import com.beeacademy.backend.infrastructure.auth.persistence.entity.ProfileJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository cho bảng 'profiles'.
 */
@Repository
public interface SpringDataProfileRepository extends JpaRepository<ProfileJpaEntity, UUID> {

    Optional<ProfileJpaEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT p FROM ProfileJpaEntity p WHERE " +
           "(:role IS NULL OR LOWER(p.role) = LOWER(:role)) AND " +
           "(:status IS NULL OR LOWER(p.status) = LOWER(:status)) AND " +
           "(:keyword IS NULL OR LOWER(p.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<ProfileJpaEntity> searchProfiles(
            @Param("role") String role,
            @Param("status") String status,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    long countByRole(String role);

    long countByStatus(String status);

    long countByTeacherApprovalStatus(String teacherApprovalStatus);
}


