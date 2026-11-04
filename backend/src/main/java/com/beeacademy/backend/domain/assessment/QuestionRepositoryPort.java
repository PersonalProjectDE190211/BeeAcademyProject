package com.beeacademy.backend.domain.assessment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Cổng Repository lưu trữ và truy vấn Câu hỏi trong ngân hàng.
 */
public interface QuestionRepositoryPort {

    Question save(Question question);

    Optional<Question> findById(UUID id);

    List<Question> findByTeacherId(UUID teacherId);

    List<Question> findByChapterId(UUID chapterId);

    List<Question> findByChapterIdAndDifficulty(UUID chapterId, QuestionDifficulty difficulty);

    List<Question> findByCategoryIdAndGrade(UUID categoryId, int grade);

    long countByChapterIdAndDifficulty(UUID chapterId, QuestionDifficulty difficulty);

    boolean existsById(UUID id);
}
