package com.beeacademy.backend.infrastructure.assessment.persistence.adapter;

import com.beeacademy.backend.domain.assessment.*;
import com.beeacademy.backend.infrastructure.assessment.persistence.entity.QuestionChoiceJpaEntity;
import com.beeacademy.backend.infrastructure.assessment.persistence.entity.QuestionJpaEntity;
import com.beeacademy.backend.infrastructure.assessment.persistence.repository.SpringDataQuestionChoiceRepository;
import com.beeacademy.backend.infrastructure.assessment.persistence.repository.SpringDataQuestionRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
public class QuestionRepositoryAdapter implements QuestionRepositoryPort {

    private final SpringDataQuestionRepository questionRepository;
    private final SpringDataQuestionChoiceRepository choiceRepository;

    public QuestionRepositoryAdapter(SpringDataQuestionRepository questionRepository,
                                   SpringDataQuestionChoiceRepository choiceRepository) {
        this.questionRepository = questionRepository;
        this.choiceRepository = choiceRepository;
    }

    @Override
    @Transactional
    public Question save(Question question) {
        QuestionJpaEntity entity = toEntity(question);
        QuestionJpaEntity saved = questionRepository.save(entity);

        // Lưu choices
        if (question.getChoices() != null) {
            choiceRepository.deleteByQuestionId(question.getId());
            for (QuestionChoice c : question.getChoices()) {
                QuestionChoiceJpaEntity choiceEntity = new QuestionChoiceJpaEntity(
                        c.getId(),
                        saved,
                        c.getContent(),
                        c.isCorrect(),
                        c.getPosition()
                );
                choiceRepository.save(choiceEntity);
            }
        }

        return findById(saved.getId()).orElse(question);
    }

    @Override
    public Optional<Question> findById(UUID id) {
        return questionRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Question> findByTeacherId(UUID teacherId) {
        return questionRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public List<Question> findByChapterId(UUID chapterId) {
        return questionRepository.findByChapterId(chapterId)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public List<Question> findByChapterIdAndDifficulty(UUID chapterId, QuestionDifficulty difficulty) {
        return questionRepository.findByChapterIdAndDifficulty(chapterId, difficulty.name().toLowerCase())
                .stream().map(this::toDomain).toList();
    }

    @Override
    public List<Question> findByCategoryIdAndGrade(UUID categoryId, int grade) {
        return questionRepository.findByCategoryIdAndGrade(categoryId, grade)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public long countByChapterIdAndDifficulty(UUID chapterId, QuestionDifficulty difficulty) {
        return questionRepository.countByChapterIdAndDifficulty(chapterId, difficulty.name().toLowerCase());
    }

    @Override
    public boolean existsById(UUID id) {
        return questionRepository.existsById(id);
    }

    private Question toDomain(QuestionJpaEntity entity) {
        List<QuestionChoice> choices = new ArrayList<>();
        if (entity.getChoices() != null) {
            for (QuestionChoiceJpaEntity c : entity.getChoices()) {
                choices.add(new QuestionChoice(
                        c.getId(),
                        entity.getId(),
                        c.getContent(),
                        null,
                        c.isCorrect(),
                        c.getPosition()
                ));
            }
        }

        QuestionDifficulty difficulty;
        try {
            difficulty = QuestionDifficulty.valueOf(entity.getDifficulty().toUpperCase());
        } catch (Exception e) {
            difficulty = QuestionDifficulty.MEDIUM;
        }

        QuestionType type;
        try {
            type = QuestionType.valueOf(entity.getType().toUpperCase());
        } catch (Exception e) {
            type = QuestionType.MULTIPLE_CHOICE;
        }

        QuestionStatus status;
        try {
            status = QuestionStatus.valueOf(entity.getStatus().toUpperCase());
        } catch (Exception e) {
            status = QuestionStatus.ACTIVE;
        }

        return new Question(
                entity.getId(),
                entity.getTeacherId(),
                entity.getCategoryId(),
                entity.getChapterId(),
                entity.getGrade(),
                entity.getContent(),
                entity.getExplanation(),
                difficulty,
                type,
                status,
                Collections.emptyList(),
                1,
                entity.getUsageCount(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                choices,
                Collections.emptyList(),
                Collections.emptyList()
        );
    }

    private QuestionJpaEntity toEntity(Question question) {
        return new QuestionJpaEntity(
                question.getId(),
                question.getTeacherId(),
                question.getCategoryId(),
                question.getChapterId(),
                question.getGrade(),
                question.getContent(),
                question.getExplanation(),
                question.getDifficulty().name().toLowerCase(),
                question.getType().name().toLowerCase(),
                question.getStatus().name().toLowerCase(),
                question.getUsageCount(),
                question.getCreatedAt(),
                question.getUpdatedAt()
        );
    }
}
