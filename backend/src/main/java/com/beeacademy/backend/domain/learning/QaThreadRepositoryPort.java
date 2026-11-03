package com.beeacademy.backend.domain.learning;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Cổng Repository lưu trữ Luồng hỏi đáp bài học (QaThread).
 */
public interface QaThreadRepositoryPort {

    QaThread save(QaThread thread);

    Optional<QaThread> findById(UUID id);

    List<QaThread> findByLessonId(UUID lessonId);

    List<QaThread> findByCourseId(UUID courseId);
}

