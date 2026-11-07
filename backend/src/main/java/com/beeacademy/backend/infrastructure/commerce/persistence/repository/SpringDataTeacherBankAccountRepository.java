package com.beeacademy.backend.infrastructure.commerce.persistence.repository;

import com.beeacademy.backend.infrastructure.commerce.persistence.entity.TeacherBankAccountJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataTeacherBankAccountRepository extends JpaRepository<TeacherBankAccountJpaEntity, UUID> {

    Optional<TeacherBankAccountJpaEntity> findByTeacherId(UUID teacherId);

    List<TeacherBankAccountJpaEntity> findByVerifyStatus(String verifyStatus);
}

