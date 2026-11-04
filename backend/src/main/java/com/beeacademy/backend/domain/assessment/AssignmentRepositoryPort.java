package com.beeacademy.backend.domain.assessment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Cổng Repository lưu trữ Bài tập tự luận (Assignment).
 */
public interface AssignmentRepositoryPort {

    Assignment save(Assignment assignment);

    Optional<Assignment> findById(UUID id);

    List<Assignment> findByLessonId(UUID lessonId);

    List<Assignment> findByTeacherId(UUID teacherId);
}
