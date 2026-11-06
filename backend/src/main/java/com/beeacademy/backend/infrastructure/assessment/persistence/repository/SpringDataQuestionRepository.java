package com.beeacademy.backend.infrastructure.assessment.persistence.repository;

import com.beeacademy.backend.infrastructure.assessment.persistence.entity.QuestionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataQuestionRepository extends JpaRepository<QuestionJpaEntity, UUID> {

    List<QuestionJpaEntity> findByTeacherIdOrderByCreatedAtDesc(UUID teacherId);

    List<QuestionJpaEntity> findByChapterId(UUID chapterId);

    List<QuestionJpaEntity> findByChapterIdAndDifficulty(UUID chapterId, String difficulty);

    List<QuestionJpaEntity> findByCategoryIdAndGrade(UUID categoryId, int grade);

    long countByChapterIdAndDifficulty(UUID chapterId, String difficulty);
}
