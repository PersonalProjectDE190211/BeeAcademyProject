package com.beeacademy.backend.application.assessment;

import com.beeacademy.backend.application.assessment.dto.QuizConfigResult;
import com.beeacademy.backend.application.assessment.dto.SaveQuizConfigCommand;
import com.beeacademy.backend.application.assessment.usecase.ConfigureQuizUseCase;
import com.beeacademy.backend.domain.assessment.QuizConfig;
import com.beeacademy.backend.domain.assessment.QuizConfigRepositoryPort;
import com.beeacademy.backend.domain.common.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DisplayName("ConfigureQuizUseCase Tests")
class ConfigureQuizUseCaseTest {

    private QuizConfigRepositoryPort quizConfigRepository;
    private ConfigureQuizUseCase useCase;

    @BeforeEach
    void setUp() {
        quizConfigRepository = Mockito.mock(QuizConfigRepositoryPort.class);
        useCase = new ConfigureQuizUseCase(quizConfigRepository);
    }

    @Test
    @DisplayName("Cấu hình bài kiểm tra hợp lệ thành công")
    void saveQuizConfig_Success() {
        UUID chapterId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();

        SaveQuizConfigCommand command = new SaveQuizConfigCommand(
                chapterId, teacherId, 10, 3, 5, 2, 30, 80.0, true, true, 3
        );

        when(quizConfigRepository.findByChapterId(chapterId)).thenReturn(Optional.empty());
        when(quizConfigRepository.save(any(QuizConfig.class))).thenAnswer(inv -> inv.getArgument(0));

        QuizConfigResult result = useCase.saveQuizConfig(command);

        assertThat(result).isNotNull();
        assertThat(result.totalQuestions()).isEqualTo(10);
        assertThat(result.passingScore()).isEqualTo(80.0);
        assertThat(result.maxAttempts()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cấu hình phân bổ số câu không khớp tổng số câu phải ném DomainException")
    void saveQuizConfig_MismatchDistribution_ThrowsException() {
        UUID chapterId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();

        // 3 + 5 + 1 != 10
        SaveQuizConfigCommand command = new SaveQuizConfigCommand(
                chapterId, teacherId, 10, 3, 5, 1, 30, 60.0, true, true, null
        );

        when(quizConfigRepository.findByChapterId(chapterId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.saveQuizConfig(command))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("phải bằng tổng số câu");
    }
}
