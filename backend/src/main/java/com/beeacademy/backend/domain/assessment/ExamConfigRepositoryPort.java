package com.beeacademy.backend.domain.assessment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Cổng Repository lưu trữ Đề thi kiểm tra chính thức (ExamConfig).
 */
public interface ExamConfigRepositoryPort {

    ExamConfig save(ExamConfig config);

    Optional<ExamConfig> findById(UUID id);

    List<ExamConfig> findByCourseId(UUID courseId);

    Optional<ExamConfig> findByCourseIdAndPlacementChapter(UUID courseId, int placementChapterIndex);
}
