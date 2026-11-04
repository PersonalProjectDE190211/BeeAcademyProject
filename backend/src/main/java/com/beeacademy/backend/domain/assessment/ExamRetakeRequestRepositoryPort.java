package com.beeacademy.backend.domain.assessment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Cổng Repository lưu trữ Đơn xin thi lại kiểm tra (ExamRetakeRequest).
 */
public interface ExamRetakeRequestRepositoryPort {

    ExamRetakeRequest save(ExamRetakeRequest request);

    Optional<ExamRetakeRequest> findById(UUID id);

    List<ExamRetakeRequest> findByExamConfigId(UUID examConfigId);

    List<ExamRetakeRequest> findByStudentIdAndExamConfigId(UUID studentId, UUID examConfigId);

    Optional<ExamRetakeRequest> findLatestByStudentIdAndExamConfigId(UUID studentId, UUID examConfigId);

    int countRequests(UUID studentId, UUID examConfigId);
}
