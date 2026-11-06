package com.beeacademy.backend.infrastructure.assessment.persistence.repository;

import com.beeacademy.backend.infrastructure.assessment.persistence.entity.QuestionChoiceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataQuestionChoiceRepository extends JpaRepository<QuestionChoiceJpaEntity, UUID> {

    List<QuestionChoiceJpaEntity> findByQuestionIdOrderByPositionAsc(UUID questionId);

    void deleteByQuestionId(UUID questionId);
}
