package com.beeacademy.backend.domain.assessment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Cổng Repository lưu trữ Bài nộp tự luận của học sinh (AssignmentSubmission).
 */
public interface AssignmentSubmissionRepositoryPort {

    AssignmentSubmission save(AssignmentSubmission submission);

    Optional<AssignmentSubmission> findById(UUID id);

    Optional<AssignmentSubmission> findByAssignmentIdAndStudentId(UUID assignmentId, UUID studentId);

    List<AssignmentSubmission> findByAssignmentId(UUID assignmentId);

    List<AssignmentSubmission> findByStudentId(UUID studentId);
}
