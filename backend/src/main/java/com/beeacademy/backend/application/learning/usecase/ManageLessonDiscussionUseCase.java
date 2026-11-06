package com.beeacademy.backend.application.learning.usecase;

import com.beeacademy.backend.application.learning.dto.AskQuestionCommand;
import com.beeacademy.backend.application.learning.dto.QaThreadResult;
import com.beeacademy.backend.application.learning.dto.ReplyQuestionCommand;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.learning.QaMessage;
import com.beeacademy.backend.domain.learning.QaThread;
import com.beeacademy.backend.domain.learning.QaThreadRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Use case Hỏi đáp Q&A dưới bài giảng giữa học viên và giáo viên (UC21, UC36).
 */
@Service
public class ManageLessonDiscussionUseCase {

    private final QaThreadRepositoryPort qaThreadRepository;

    public ManageLessonDiscussionUseCase(QaThreadRepositoryPort qaThreadRepository) {
        this.qaThreadRepository = qaThreadRepository;
    }

    @Transactional(readOnly = true)
    public List<QaThreadResult> getLessonDiscussions(UUID lessonId) {
        return qaThreadRepository.findByLessonId(lessonId).stream()
                .map(QaThreadResult::from)
                .toList();
    }

    @Transactional
    public QaThreadResult askQuestion(AskQuestionCommand command) {
        if (command == null) {
            throw new DomainException("INVALID_COMMAND", "Dữ liệu câu hỏi không được rỗng");
        }

        QaThread thread = QaThread.open(
                command.studentId(),
                command.courseId(),
                command.lessonId(),
                command.title(),
                command.content()
        );

        QaThread saved = qaThreadRepository.save(thread);
        return QaThreadResult.from(saved);
    }

    @Transactional
    public QaThreadResult replyQuestion(ReplyQuestionCommand command) {
        if (command == null) {
            throw new DomainException("INVALID_COMMAND", "Dữ liệu phản hồi không được rỗng");
        }

        QaThread thread = qaThreadRepository.findById(command.threadId())
                .orElseThrow(() -> new DomainException("THREAD_NOT_FOUND", "Không tìm thấy luồng thảo luận"));

        thread.addReply(command.senderId(), command.content());
        QaThread saved = qaThreadRepository.save(thread);
        return QaThreadResult.from(saved);
    }
}
