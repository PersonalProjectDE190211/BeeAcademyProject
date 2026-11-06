package com.beeacademy.backend.application.assessment.usecase;

import com.beeacademy.backend.application.assessment.dto.*;
import com.beeacademy.backend.domain.assessment.*;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.common.ScorePercent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

/**
 * Use case Học viên bắt đầu làm bài kiểm tra và Nộp bài chấm điểm tự động (UC17, UC18, UC19).
 */
@Service
public class TakeQuizUseCase {

    private final QuizConfigRepositoryPort quizConfigRepository;
    private final QuizAttemptRepositoryPort quizAttemptRepository;
    private final QuestionRepositoryPort questionRepository;

    public TakeQuizUseCase(QuizConfigRepositoryPort quizConfigRepository,
                           QuizAttemptRepositoryPort quizAttemptRepository,
                           QuestionRepositoryPort questionRepository) {
        this.quizConfigRepository = quizConfigRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.questionRepository = questionRepository;
    }

    /**
     * UC17: Bắt đầu làm bài kiểm tra trắc nghiệm của một chương.
     * Tự động lấy câu hỏi theo ma trận độ khó, xáo trộn đề và ẩn đáp án đúng.
     */
    @Transactional
    public StartQuizResult startQuiz(UUID studentId, UUID chapterId) {
        QuizConfig config = quizConfigRepository.findByChapterId(chapterId)
                .orElseThrow(() -> new DomainException("QUIZ_NOT_CONFIGURED", "Chương này chưa có bài kiểm tra trắc nghiệm"));

        // Kiểm tra số lần làm lại tối đa
        if (config.getMaxAttempts() != null && config.getMaxAttempts() > 0) {
            long attemptCount = quizAttemptRepository.countAttempts(studentId, config.getId());
            if (attemptCount >= config.getMaxAttempts()) {
                throw new DomainException("MAX_ATTEMPTS_EXCEEDED", "Bạn đã hết số lượt làm bài cho phép (" + config.getMaxAttempts() + " lần)");
            }
        }

        // Lấy câu hỏi từ ngân hàng câu hỏi
        List<Question> selectedQuestions = selectQuestions(config);
        if (selectedQuestions.isEmpty()) {
            throw new DomainException("INSUFFICIENT_QUESTIONS", "Ngân hàng câu hỏi của chương này chưa đủ dữ liệu để tạo đề thi");
        }

        if (config.isShuffleQuestions()) {
            Collections.shuffle(selectedQuestions);
        }

        // Khởi tạo lượt làm bài
        QuizAttempt attempt = QuizAttempt.start(studentId, config.getId(), chapterId);
        QuizAttempt savedAttempt = quizAttemptRepository.save(attempt);

        // Tạo cấu trúc câu hỏi cho học viên (ẨN đáp án đúng)
        List<ExamQuestionItem> examQuestions = new ArrayList<>();
        for (Question q : selectedQuestions) {
            List<ExamChoiceItem> examChoices = new ArrayList<>();
            if (q.getChoices() != null) {
                for (QuestionChoice c : q.getChoices()) {
                    examChoices.add(new ExamChoiceItem(c.getId(), c.getContent(), c.getImageUrl(), c.getPosition()));
                }
            }
            if (config.isShuffleChoices()) {
                Collections.shuffle(examChoices);
            }
            examQuestions.add(new ExamQuestionItem(q.getId(), q.getContent(), q.getType().name(), examChoices));
        }

        return new StartQuizResult(
                savedAttempt.getId(),
                chapterId,
                examQuestions.size(),
                config.getTimeLimitMinutes(),
                config.getPassingScore().value().doubleValue(),
                savedAttempt.getStartedAt(),
                examQuestions
        );
    }

    /**
     * UC17, UC18: Nộp bài kiểm tra và tự động tính điểm tức thì.
     */
    @Transactional
    public QuizResult submitQuiz(SubmitQuizCommand command) {
        if (command == null || command.attemptId() == null) {
            throw new DomainException("INVALID_COMMAND", "Dữ liệu nộp bài không hợp lệ");
        }

        QuizAttempt attempt = quizAttemptRepository.findById(command.attemptId())
                .orElseThrow(() -> new DomainException("ATTEMPT_NOT_FOUND", "Không tìm thấy lượt làm bài"));

        if (!attempt.getStudentId().equals(command.studentId())) {
            throw new DomainException("FORBIDDEN", "Bạn không có quyền nộp bài cho lượt làm của học viên khác");
        }

        QuizConfig config = quizConfigRepository.findById(attempt.getQuizConfigId())
                .orElseThrow(() -> new DomainException("QUIZ_CONFIG_NOT_FOUND", "Không tìm thấy cấu hình bài kiểm tra"));

        // Lấy danh sách câu hỏi của chương
        List<Question> chapterQuestions = questionRepository.findByChapterId(attempt.getChapterId());
        Map<UUID, Question> questionMap = new HashMap<>();
        for (Question q : chapterQuestions) {
            questionMap.put(q.getId(), q);
        }

        Map<UUID, UUID> studentAnswers = command.answers() != null ? command.answers() : Collections.emptyMap();
        int correctCount = 0;
        int totalQuestions = 0;
        List<QuestionReviewItem> reviews = new ArrayList<>();

        for (Map.Entry<UUID, UUID> entry : studentAnswers.entrySet()) {
            UUID questionId = entry.getKey();
            UUID selectedChoiceId = entry.getValue();

            Question question = questionMap.get(questionId);
            if (question == null) {
                question = questionRepository.findById(questionId).orElse(null);
            }

            if (question != null) {
                totalQuestions++;
                UUID correctChoiceId = null;
                if (question.getChoices() != null) {
                    for (QuestionChoice c : question.getChoices()) {
                        if (c.isCorrect()) {
                            correctChoiceId = c.getId();
                            break;
                        }
                    }
                }

                boolean isCorrect = selectedChoiceId != null && selectedChoiceId.equals(correctChoiceId);
                if (isCorrect) {
                    correctCount++;
                }

                reviews.add(new QuestionReviewItem(
                        question.getId(),
                        question.getContent(),
                        question.getExplanation(),
                        selectedChoiceId,
                        correctChoiceId,
                        isCorrect
                ));
            }
        }

        // Tính điểm phần trăm
        int gradedTotal = Math.max(totalQuestions, 1);
        ScorePercent calculatedScore = ScorePercent.fromRatio(correctCount, gradedTotal);

        attempt.submit(calculatedScore, config.getPassingScore(), Instant.now());
        quizAttemptRepository.save(attempt);

        return new QuizResult(
                attempt.getId(),
                attempt.getStudentId(),
                attempt.getChapterId(),
                calculatedScore.value().doubleValue(),
                attempt.isPassed(),
                gradedTotal,
                correctCount,
                attempt.getSubmittedAt(),
                reviews
        );
    }

    private List<Question> selectQuestions(QuizConfig config) {
        List<Question> allInChapter = questionRepository.findByChapterId(config.getChapterId());
        if (allInChapter.size() <= config.getTotalQuestions()) {
            return new ArrayList<>(allInChapter);
        }

        List<Question> easy = questionRepository.findByChapterIdAndDifficulty(config.getChapterId(), QuestionDifficulty.EASY);
        List<Question> medium = questionRepository.findByChapterIdAndDifficulty(config.getChapterId(), QuestionDifficulty.MEDIUM);
        List<Question> hard = questionRepository.findByChapterIdAndDifficulty(config.getChapterId(), QuestionDifficulty.HARD);

        List<Question> result = new ArrayList<>();
        addRandomSublist(result, easy, config.getEasyCount());
        addRandomSublist(result, medium, config.getMediumCount());
        addRandomSublist(result, hard, config.getHardCount());

        // Bổ sung nếu chưa đủ
        if (result.size() < config.getTotalQuestions()) {
            for (Question q : allInChapter) {
                if (!result.contains(q)) {
                    result.add(q);
                    if (result.size() == config.getTotalQuestions()) break;
                }
            }
        }
        return result;
    }

    private void addRandomSublist(List<Question> target, List<Question> source, int count) {
        if (source == null || source.isEmpty() || count <= 0) return;
        List<Question> copy = new ArrayList<>(source);
        Collections.shuffle(copy);
        int take = Math.min(count, copy.size());
        target.addAll(copy.subList(0, take));
    }
}
