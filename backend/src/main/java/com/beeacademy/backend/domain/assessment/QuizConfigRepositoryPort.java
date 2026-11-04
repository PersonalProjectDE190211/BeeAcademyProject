package com.beeacademy.backend.domain.assessment;

import java.util.Optional;
import java.util.UUID;

/**
 * Cổng Repository lưu trữ Cấu hình quiz chương.
 */
public interface QuizConfigRepositoryPort {

    QuizConfig save(QuizConfig config);

    Optional<QuizConfig> findById(UUID id);

    Optional<QuizConfig> findByChapterId(UUID chapterId);

    boolean existsByChapterId(UUID chapterId);
}
