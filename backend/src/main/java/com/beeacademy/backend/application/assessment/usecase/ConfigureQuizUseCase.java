package com.beeacademy.backend.application.assessment.usecase;

import com.beeacademy.backend.application.assessment.dto.QuizConfigResult;
import com.beeacademy.backend.application.assessment.dto.SaveQuizConfigCommand;
import com.beeacademy.backend.domain.assessment.QuizConfig;
import com.beeacademy.backend.domain.assessment.QuizConfigRepositoryPort;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.common.ScorePercent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case Cấu hình bài kiểm tra trắc nghiệm từng chương của giáo viên (UC33).
 */
@Service
public class ConfigureQuizUseCase {

    private final QuizConfigRepositoryPort quizConfigRepository;

    public ConfigureQuizUseCase(QuizConfigRepositoryPort quizConfigRepository) {
        this.quizConfigRepository = quizConfigRepository;
    }

    /**
     * Lấy cấu hình bài kiểm tra theo ID chương.
     */
    @Transactional(readOnly = true)
    public QuizConfigResult getQuizConfig(UUID chapterId) {
        QuizConfig config = quizConfigRepository.findByChapterId(chapterId)
                .orElseThrow(() -> new DomainException("QUIZ_CONFIG_NOT_FOUND", "Chương này chưa được thiết lập bài kiểm tra"));
        return QuizConfigResult.from(config);
    }

    /**
     * Lưu hoặc cập nhật cấu hình bài kiểm tra của một chương.
     */
    @Transactional
    public QuizConfigResult saveQuizConfig(SaveQuizConfigCommand command) {
        if (command == null) {
            throw new DomainException("INVALID_COMMAND", "Dữ liệu cấu hình bài kiểm tra không được rỗng");
        }

        ScorePercent passing = ScorePercent.of(command.passingScore() > 0 ? command.passingScore() : 60.0);

        QuizConfig config = quizConfigRepository.findByChapterId(command.chapterId())
                .map(existing -> {
                    existing.update(
                            command.totalQuestions(),
                            command.easyCount(),
                            command.mediumCount(),
                            command.hardCount(),
                            command.timeLimitMinutes(),
                            passing,
                            command.shuffleQuestions(),
                            command.shuffleChoices(),
                            command.maxAttempts()
                    );
                    return existing;
                })
                .orElseGet(() -> QuizConfig.create(
                        command.chapterId(),
                        command.teacherId(),
                        command.totalQuestions(),
                        command.easyCount(),
                        command.mediumCount(),
                        command.hardCount(),
                        command.timeLimitMinutes(),
                        passing,
                        command.shuffleQuestions(),
                        command.shuffleChoices(),
                        command.maxAttempts()
                ));

        QuizConfig saved = quizConfigRepository.save(config);
        return QuizConfigResult.from(saved);
    }
}
