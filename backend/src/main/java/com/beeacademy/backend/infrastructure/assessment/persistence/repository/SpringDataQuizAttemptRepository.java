package com.beeacademy.backend.infrastructure.assessment.persistence.repository;

import com.beeacademy.backend.infrastructure.assessment.persistence.entity.QuizAttemptJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataQuizAttemptRepository extends JpaRepository<QuizAttemptJpaEntity, UUID> {

    List<QuizAttemptJpaEntity> findByStudentIdOrderByStartedAtDesc(UUID studentId);

    @Query("SELECT a FROM QuizAttemptJpaEntity a JOIN QuizConfigJpaEntity c ON a.quizConfigId = c.id WHERE a.studentId = :studentId AND c.chapterId = :chapterId ORDER BY a.startedAt DESC")
    List<QuizAttemptJpaEntity> findByStudentIdAndChapterId(@Param("studentId") UUID studentId, @Param("chapterId") UUID chapterId);

    long countByStudentIdAndQuizConfigId(UUID studentId, UUID quizConfigId);

    @Query("SELECT COUNT(a) > 0 FROM QuizAttemptJpaEntity a JOIN QuizConfigJpaEntity c ON a.quizConfigId = c.id WHERE a.studentId = :studentId AND c.chapterId = :chapterId AND a.passed = true")
    boolean hasPassedChapterQuiz(@Param("studentId") UUID studentId, @Param("chapterId") UUID chapterId);
}
