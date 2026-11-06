package com.beeacademy.backend.infrastructure.assessment.persistence.repository;

import com.beeacademy.backend.infrastructure.assessment.persistence.entity.QuizConfigJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataQuizConfigRepository extends JpaRepository<QuizConfigJpaEntity, UUID> {

    Optional<QuizConfigJpaEntity> findByChapterId(UUID chapterId);

    boolean existsByChapterId(UUID chapterId);
}
