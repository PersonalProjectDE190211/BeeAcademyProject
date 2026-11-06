package com.beeacademy.backend.application.assessment;

import com.beeacademy.backend.application.assessment.dto.QuizResult;
import com.beeacademy.backend.application.assessment.dto.StartQuizResult;
import com.beeacademy.backend.application.assessment.dto.SubmitQuizCommand;
import com.beeacademy.backend.application.assessment.usecase.TakeQuizUseCase;
import com.beeacademy.backend.domain.assessment.*;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.common.ScorePercent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DisplayName("TakeQuizUseCase Tests")
class TakeQuizUseCaseTest {

    private QuizConfigRepositoryPort quizConfigRepository;
    private QuizAttemptRepositoryPort quizAttemptRepository;
    private QuestionRepositoryPort questionRepository;
    private TakeQuizUseCase useCase;

    private final UUID studentId = UUID.randomUUID();
    private final UUID chapterId = UUID.randomUUID();
    private final UUID teacherId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        quizConfigRepository = Mockito.mock(QuizConfigRepositoryPort.class);
        quizAttemptRepository = Mockito.mock(QuizAttemptRepositoryPort.class);
        questionRepository = Mockito.mock(QuestionRepositoryPort.class);
        useCase = new TakeQuizUseCase(quizConfigRepository, quizAttemptRepository, questionRepository);
    }

    @Test
    @DisplayName("Học sinh bắt đầu làm quiz thành công: đề được tạo và ẩn đáp án đúng")
    void startQuiz_Success() {
        QuizConfig config = QuizConfig.create(chapterId, teacherId, 2, 1, 1, 0, 15, ScorePercent.of(80.0), true, true, 2);
        when(quizConfigRepository.findByChapterId(chapterId)).thenReturn(Optional.of(config));
        when(quizAttemptRepository.countAttempts(studentId, config.getId())).thenReturn(0L);

        Question q1 = Question.create(teacherId, UUID.randomUUID(), chapterId, 8, "Câu 1?", "GT 1",
                QuestionDifficulty.EASY, QuestionType.MULTIPLE_CHOICE, List.of(),
                List.of(QuestionChoice.ofText(UUID.randomUUID(), "Đúng", true, 1), QuestionChoice.ofText(UUID.randomUUID(), "Sai", false, 2)));

        Question q2 = Question.create(teacherId, UUID.randomUUID(), chapterId, 8, "Câu 2?", "GT 2",
                QuestionDifficulty.MEDIUM, QuestionType.MULTIPLE_CHOICE, List.of(),
                List.of(QuestionChoice.ofText(UUID.randomUUID(), "Đúng", true, 1), QuestionChoice.ofText(UUID.randomUUID(), "Sai", false, 2)));

        when(questionRepository.findByChapterId(chapterId)).thenReturn(List.of(q1, q2));
        when(quizAttemptRepository.save(any(QuizAttempt.class))).thenAnswer(inv -> inv.getArgument(0));

        StartQuizResult result = useCase.startQuiz(studentId, chapterId);

        assertThat(result).isNotNull();
        assertThat(result.totalQuestions()).isEqualTo(2);
        assertThat(result.questions()).hasSize(2);
        // Kiểm tra đề thi đã ẩn trường isCorrect
        assertThat(result.questions().get(0).choices().get(0).content()).isNotBlank();
    }

    @Test
    @DisplayName("Nộp bài kiểm tra đạt điểm chuẩn thì passed = true")
    void submitQuiz_Pass() {
        UUID attemptId = UUID.randomUUID();
        UUID configId = UUID.randomUUID();
        QuizAttempt attempt = new QuizAttempt(attemptId, studentId, configId, chapterId, ScorePercent.ZERO, false, AttemptStatus.IN_PROGRESS, null, null);
        QuizConfig config = QuizConfig.create(chapterId, teacherId, 2, 1, 1, 0, 15, ScorePercent.of(50.0), false, false, 2);

        when(quizAttemptRepository.findById(attemptId)).thenReturn(Optional.of(attempt));
        when(quizConfigRepository.findById(configId)).thenReturn(Optional.of(config));

        UUID q1Id = UUID.randomUUID();
        UUID correctChoiceId = UUID.randomUUID();
        QuestionChoice choice1 = new QuestionChoice(correctChoiceId, q1Id, "A", null, true, 1);
        QuestionChoice choice2 = new QuestionChoice(UUID.randomUUID(), q1Id, "B", null, false, 2);

        Question q1 = new Question(q1Id, teacherId, UUID.randomUUID(), chapterId, 8, "Câu 1", "GT",
                QuestionDifficulty.EASY, QuestionType.MULTIPLE_CHOICE, QuestionStatus.ACTIVE,
                List.of(), 1, 0, null, null, List.of(choice1, choice2), List.of(), List.of());

        when(questionRepository.findByChapterId(chapterId)).thenReturn(List.of(q1));
        when(quizAttemptRepository.save(any(QuizAttempt.class))).thenAnswer(inv -> inv.getArgument(0));

        SubmitQuizCommand command = new SubmitQuizCommand(attemptId, studentId, Map.of(q1Id, correctChoiceId));
        QuizResult result = useCase.submitQuiz(command);

        assertThat(result).isNotNull();
        assertThat(result.score()).isEqualTo(100.0);
        assertThat(result.passed()).isTrue();
        assertThat(result.correctCount()).isEqualTo(1);
    }
}
