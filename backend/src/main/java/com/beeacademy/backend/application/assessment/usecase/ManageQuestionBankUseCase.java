package com.beeacademy.backend.application.assessment.usecase;

import com.beeacademy.backend.application.assessment.dto.*;
import com.beeacademy.backend.domain.assessment.*;
import com.beeacademy.backend.domain.common.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Use case Quản lý ngân hàng câu hỏi của giáo viên (UC34).
 */
@Service
public class ManageQuestionBankUseCase {

    private final QuestionRepositoryPort questionRepository;

    public ManageQuestionBankUseCase(QuestionRepositoryPort questionRepository) {
        this.questionRepository = questionRepository;
    }

    /**
     * Tạo mới một câu hỏi vào ngân hàng câu hỏi.
     */
    @Transactional
    public QuestionResult createQuestion(CreateQuestionCommand command) {
        if (command == null) {
            throw new DomainException("INVALID_COMMAND", "Dữ liệu câu hỏi không được rỗng");
        }

        QuestionDifficulty difficulty;
        try {
            difficulty = QuestionDifficulty.valueOf(command.difficulty().toUpperCase());
        } catch (Exception e) {
            difficulty = QuestionDifficulty.MEDIUM;
        }

        QuestionType type;
        try {
            type = QuestionType.valueOf(command.type().toUpperCase());
        } catch (Exception e) {
            type = QuestionType.MULTIPLE_CHOICE;
        }

        List<QuestionChoice> choices = new ArrayList<>();
        if (command.choices() != null) {
            UUID tempQId = UUID.randomUUID();
            int pos = 1;
            for (ChoiceItem c : command.choices()) {
                choices.add(new QuestionChoice(
                        UUID.randomUUID(),
                        tempQId,
                        c.content(),
                        c.imageUrl(),
                        c.isCorrect(),
                        c.position() > 0 ? c.position() : pos++
                ));
            }
        }

        Question question = Question.create(
                command.teacherId(),
                command.categoryId(),
                command.chapterId(),
                command.grade(),
                command.content(),
                command.explanation(),
                difficulty,
                type,
                command.tags(),
                choices
        );

        Question saved = questionRepository.save(question);
        return QuestionResult.from(saved);
    }

    /**
     * Lấy danh sách câu hỏi do giáo viên tạo.
     */
    @Transactional(readOnly = true)
    public List<QuestionResult> getQuestionsByTeacher(UUID teacherId) {
        return questionRepository.findByTeacherId(teacherId)
                .stream().map(QuestionResult::from).toList();
    }

    /**
     * Lấy danh sách câu hỏi thuộc một chương học.
     */
    @Transactional(readOnly = true)
    public List<QuestionResult> getQuestionsByChapter(UUID chapterId) {
        return questionRepository.findByChapterId(chapterId)
                .stream().map(QuestionResult::from).toList();
    }

    /**
     * Lấy chi tiết một câu hỏi theo ID.
     */
    @Transactional(readOnly = true)
    public QuestionResult getQuestionDetail(UUID questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new DomainException("QUESTION_NOT_FOUND", "Không tìm thấy câu hỏi với ID: " + questionId));
        return QuestionResult.from(question);
    }

    /**
     * Vô hiệu hóa một câu hỏi.
     */
    @Transactional
    public void deactivateQuestion(UUID questionId, UUID teacherId, String reason) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new DomainException("QUESTION_NOT_FOUND", "Không tìm thấy câu hỏi với ID: " + questionId));
        question.deactivate(teacherId, reason);
        questionRepository.save(question);
    }
}
