package com.beeacademy.backend.infrastructure.assessment.persistence.adapter;

import com.beeacademy.backend.domain.assessment.AttemptStatus;
import com.beeacademy.backend.domain.assessment.QuizAttempt;
import com.beeacademy.backend.domain.assessment.QuizAttemptRepositoryPort;
import com.beeacademy.backend.domain.common.ScorePercent;
import com.beeacademy.backend.infrastructure.assessment.persistence.entity.QuizAttemptJpaEntity;
import com.beeacademy.backend.infrastructure.assessment.persistence.entity.QuizConfigJpaEntity;
import com.beeacademy.backend.infrastructure.assessment.persistence.repository.SpringDataQuizAttemptRepository;
import com.beeacademy.backend.infrastructure.assessment.persistence.repository.SpringDataQuizConfigRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class QuizAttemptRepositoryAdapter implements QuizAttemptRepositoryPort {

    private final SpringDataQuizAttemptRepository attemptRepository;
    private final SpringDataQuizConfigRepository configRepository;

    public QuizAttemptRepositoryAdapter(SpringDataQuizAttemptRepository attemptRepository,
                                       SpringDataQuizConfigRepository configRepository) {
        this.attemptRepository = attemptRepository;
        this.configRepository = configRepository;
    }

    @Override
    public QuizAttempt save(QuizAttempt attempt) {
        QuizAttemptJpaEntity entity = toEntity(attempt);
        QuizAttemptJpaEntity saved = attemptRepository.save(entity);
        return toDomain(saved, attempt.getChapterId());
    }

    @Override
    public Optional<QuizAttempt> findById(UUID id) {
        return attemptRepository.findById(id).map(entity -> {
            UUID chapterId = configRepository.findById(entity.getQuizConfigId())
                    .map(QuizConfigJpaEntity::getChapterId)
                    .orElse(UUID.randomUUID());
            return toDomain(entity, chapterId);
        });
    }

    @Override
    public List<QuizAttempt> findByStudentIdAndChapterId(UUID studentId, UUID chapterId) {
        return attemptRepository.findByStudentIdAndChapterId(studentId, chapterId)
                .stream()
                .map(e -> toDomain(e, chapterId))
                .toList();
    }

    @Override
    public long countAttempts(UUID studentId, UUID quizConfigId) {
        return attemptRepository.countByStudentIdAndQuizConfigId(studentId, quizConfigId);
    }

    @Override
    public boolean hasPassedChapterQuiz(UUID studentId, UUID chapterId) {
        return attemptRepository.hasPassedChapterQuiz(studentId, chapterId);
    }

    private QuizAttempt toDomain(QuizAttemptJpaEntity entity, UUID chapterId) {
        AttemptStatus status;
        if (entity.getSubmittedAt() != null) {
            status = AttemptStatus.SUBMITTED;
        } else {
            status = AttemptStatus.IN_PROGRESS;
        }

        ScorePercent score = entity.getScore() != null
                ? ScorePercent.of(entity.getScore().doubleValue())
                : ScorePercent.ZERO;

        boolean passed = entity.getPassed() != null && entity.getPassed();

        return new QuizAttempt(
                entity.getId(),
                entity.getStudentId(),
                entity.getQuizConfigId(),
                chapterId,
                score,
                passed,
                status,
                entity.getStartedAt(),
                entity.getSubmittedAt()
        );
    }

    private QuizAttemptJpaEntity toEntity(QuizAttempt attempt) {
        BigDecimal scoreVal = attempt.getScore() != null
                ? attempt.getScore().value()
                : null;

        return new QuizAttemptJpaEntity(
                attempt.getId(),
                attempt.getStudentId(),
                attempt.getQuizConfigId(),
                "[]",
                "{}",
                scoreVal,
                attempt.isPassed(),
                1,
                attempt.getStartedAt(),
                attempt.getSubmittedAt()
        );
    }
}
