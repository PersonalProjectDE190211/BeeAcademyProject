package com.beeacademy.backend.domain.learning;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Cổng Repository lưu trữ Ghi chú bài học của học viên (StudentLessonNote).
 */
public interface StudentLessonNoteRepositoryPort {

    StudentLessonNote save(StudentLessonNote note);

    Optional<StudentLessonNote> findById(UUID id);

    List<StudentLessonNote> findByStudentIdAndLessonId(UUID studentId, UUID lessonId);

    void deleteById(UUID id);
}

