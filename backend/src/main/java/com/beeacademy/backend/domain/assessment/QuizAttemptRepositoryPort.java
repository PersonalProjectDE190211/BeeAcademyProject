package com.beeacademy.backend.domain.assessment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Cổng Repository lưu trữ Lượt làm quiz chương của học sinh.
 */
public interface QuizAttemptRepositoryPort {

    QuizAttempt save(QuizAttempt attempt);

    Optional<QuizAttempt> findById(UUID id);

    List<QuizAttempt> findByStudentIdAndChapterId(UUID studentId, UUID chapterId);

    long countAttempts(UUID studentId, UUID quizConfigId);

    boolean hasPassedChapterQuiz(UUID studentId, UUID chapterId);
}
