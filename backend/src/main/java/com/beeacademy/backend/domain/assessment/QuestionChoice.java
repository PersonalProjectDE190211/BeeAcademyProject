package com.beeacademy.backend.domain.assessment;

import com.beeacademy.backend.domain.common.DomainException;

import java.util.UUID;

/**
 * Entity đại diện cho một phương án trả lời của câu hỏi trắc nghiệm.
 * Hỗ trợ công thức LaTeX trong nội dung và đính kèm URL ảnh phương án (ví dụ hình vẽ hình học).
 */
public class QuestionChoice {

    private final UUID id;
    private final UUID questionId;
    private final String content;
    private final String imageUrl;
    private final boolean correct;
    private final int position;

    public QuestionChoice(UUID id, UUID questionId, String content, String imageUrl, boolean correct, int position) {
        if (id == null) throw new DomainException("INVALID_ID", "Choice ID không được null");
        if (questionId == null) throw new DomainException("INVALID_QUESTION_ID", "Question ID không được null");
        if ((content == null || content.isBlank()) && (imageUrl == null || imageUrl.isBlank())) {
            throw new DomainException("INVALID_CHOICE_CONTENT", "Phương án phải có ít nhất nội dung văn bản hoặc hình ảnh");
        }

        this.id = id;
        this.questionId = questionId;
        this.content = content != null ? content.trim() : "";
        this.imageUrl = imageUrl != null ? imageUrl.trim() : "";
        this.correct = correct;
        this.position = Math.max(1, position);
    }

    public static QuestionChoice of(UUID questionId, String content, String imageUrl, boolean correct, int position) {
        return new QuestionChoice(UUID.randomUUID(), questionId, content, imageUrl, correct, position);
    }

    public static QuestionChoice ofText(UUID questionId, String content, boolean correct, int position) {
        return new QuestionChoice(UUID.randomUUID(), questionId, content, null, correct, position);
    }

    public UUID getId() { return id; }
    public UUID getQuestionId() { return questionId; }
    public String getContent() { return content; }
    public String getImageUrl() { return imageUrl; }
    public boolean isCorrect() { return correct; }
    public int getPosition() { return position; }
}
