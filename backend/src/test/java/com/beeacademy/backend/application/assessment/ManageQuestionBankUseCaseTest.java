package com.beeacademy.backend.application.assessment;

import com.beeacademy.backend.application.assessment.dto.ChoiceItem;
import com.beeacademy.backend.application.assessment.dto.CreateQuestionCommand;
import com.beeacademy.backend.application.assessment.dto.QuestionResult;
import com.beeacademy.backend.application.assessment.usecase.ManageQuestionBankUseCase;
import com.beeacademy.backend.domain.assessment.Question;
import com.beeacademy.backend.domain.assessment.QuestionRepositoryPort;
import com.beeacademy.backend.domain.common.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DisplayName("ManageQuestionBankUseCase Tests")
class ManageQuestionBankUseCaseTest {

    private QuestionRepositoryPort questionRepository;
    private ManageQuestionBankUseCase useCase;

    @BeforeEach
    void setUp() {
        questionRepository = Mockito.mock(QuestionRepositoryPort.class);
        useCase = new ManageQuestionBankUseCase(questionRepository);
    }

    @Test
    @DisplayName("Tạo câu hỏi hợp lệ thành công và lưu vào ngân hàng")
    void createQuestion_Success() {
        UUID teacherId = UUID.randomUUID();
        UUID chapterId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        List<ChoiceItem> choices = List.of(
                new ChoiceItem("Phương án A", null, true, 1),
                new ChoiceItem("Phương án B", null, false, 2)
        );

        CreateQuestionCommand command = new CreateQuestionCommand(
                teacherId, categoryId, chapterId, 8,
                "Đâu là từ khóa khai báo hằng trong Java?",
                "final là từ khóa khai báo hằng",
                "EASY", "MULTIPLE_CHOICE", choices, List.of("java", "oop")
        );

        when(questionRepository.save(any(Question.class))).thenAnswer(invocation -> invocation.getArgument(0));

        QuestionResult result = useCase.createQuestion(command);

        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo("Đâu là từ khóa khai báo hằng trong Java?");
        assertThat(result.choices()).hasSize(2);
        assertThat(result.difficulty()).isEqualTo("EASY");
    }

    @Test
    @DisplayName("Tạo câu hỏi trắc nghiệm không có đáp án đúng phải ném DomainException")
    void createQuestion_NoCorrectChoice_ThrowsException() {
        UUID teacherId = UUID.randomUUID();
        List<ChoiceItem> choices = List.of(
                new ChoiceItem("Phương án A", null, false, 1),
                new ChoiceItem("Phương án B", null, false, 2)
        );

        CreateQuestionCommand command = new CreateQuestionCommand(
                teacherId, UUID.randomUUID(), UUID.randomUUID(), 8,
                "Câu hỏi không có đáp án đúng", "Giải thích",
                "EASY", "MULTIPLE_CHOICE", choices, List.of()
        );

        assertThatThrownBy(() -> useCase.createQuestion(command))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("ít nhất 1 phương án đúng");
    }
}
