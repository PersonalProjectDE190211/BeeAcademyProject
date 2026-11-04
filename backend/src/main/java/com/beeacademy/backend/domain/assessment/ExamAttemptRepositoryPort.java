package com.beeacademy.backend.domain.assessment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Cổng Repository lưu trữ Lượt làm bài thi kiểm tra của học sinh (ExamAttempt).
 */
public interface ExamAttemptRepositoryPort {

    ExamAttempt save(ExamAttempt attempt);

    Optional<ExamAttempt> findById(UUID id);

    List<ExamAttempt> findByStudentIdAndExamConfigId(UUID studentId, UUID examConfigId);

    int countAttempts(UUID studentId, UUID examConfigId);

    boolean hasPassedExam(UUID studentId, UUID examConfigId);
}
