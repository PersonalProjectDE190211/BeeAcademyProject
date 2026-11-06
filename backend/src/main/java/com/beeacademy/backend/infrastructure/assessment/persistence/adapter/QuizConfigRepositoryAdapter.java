package com.beeacademy.backend.infrastructure.assessment.persistence.adapter;

import com.beeacademy.backend.domain.assessment.QuizConfig;
import com.beeacademy.backend.domain.assessment.QuizConfigRepositoryPort;
import com.beeacademy.backend.domain.common.ScorePercent;
import com.beeacademy.backend.infrastructure.assessment.persistence.entity.QuizConfigJpaEntity;
import com.beeacademy.backend.infrastructure.assessment.persistence.repository.SpringDataQuizConfigRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
public class QuizConfigRepositoryAdapter implements QuizConfigRepositoryPort {

    private final SpringDataQuizConfigRepository quizConfigRepository;

    public QuizConfigRepositoryAdapter(SpringDataQuizConfigRepository quizConfigRepository) {
        this.quizConfigRepository = quizConfigRepository;
    }

    @Override
    public QuizConfig save(QuizConfig config) {
        QuizConfigJpaEntity entity = toEntity(config);
        QuizConfigJpaEntity saved = quizConfigRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<QuizConfig> findById(UUID id) {
        return quizConfigRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<QuizConfig> findByChapterId(UUID chapterId) {
        return quizConfigRepository.findByChapterId(chapterId).map(this::toDomain);
    }

    @Override
    public boolean existsByChapterId(UUID chapterId) {
        return quizConfigRepository.existsByChapterId(chapterId);
    }

    private QuizConfig toDomain(QuizConfigJpaEntity entity) {
        return new QuizConfig(
                entity.getId(),
                entity.getChapterId(),
                entity.getTeacherId(),
                entity.getTotalQuestions(),
                entity.getEasyCount(),
                entity.getMediumCount(),
                entity.getHardCount(),
                entity.getTimeLimitMinutes(),
                ScorePercent.of(entity.getPassingScore()),
                entity.isShuffleQuestions(),
                entity.isShuffleChoices(),
                entity.getMaxAttempts()
        );
    }

    private QuizConfigJpaEntity toEntity(QuizConfig config) {
        Instant now = Instant.now();
        return new QuizConfigJpaEntity(
                config.getId(),
                config.getChapterId(),
                config.getTeacherId(),
                config.getTotalQuestions(),
                config.getEasyCount(),
                config.getMediumCount(),
                config.getHardCount(),
                config.getTimeLimitMinutes(),
                config.getPassingScore().value(),
                config.isShuffleQuestions(),
                config.isShuffleChoices(),
                config.getMaxAttempts(),
                now,
                now
        );
    }
}
