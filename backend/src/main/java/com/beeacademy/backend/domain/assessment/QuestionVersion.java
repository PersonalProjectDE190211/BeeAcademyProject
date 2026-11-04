package com.beeacademy.backend.domain.assessment;

import com.beeacademy.backend.domain.common.DomainException;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity lưu vết snapshot lịch sử của câu hỏi qua từng lần sửa đổi (UC33).
 */
public class QuestionVersion {

    private final UUID id;
    private final UUID questionId;
    private final int versionNo;
    private final String content;
    private final String explanation;
    private final String choicesSnapshotJson;
    private final Instant createdAt;

    public QuestionVersion(UUID id, UUID questionId, int versionNo, String content, String explanation, String choicesSnapshotJson, Instant createdAt) {
        if (id == null) throw new DomainException("INVALID_ID", "ID không được null");
        if (questionId == null) throw new DomainException("INVALID_QUESTION_ID", "Question ID không được null");
        if (versionNo < 1) throw new DomainException("INVALID_VERSION_NO", "Version number phải >= 1");

        this.id = id;
        this.questionId = questionId;
        this.versionNo = versionNo;
        this.content = content != null ? content : "";
        this.explanation = explanation != null ? explanation : "";
        this.choicesSnapshotJson = choicesSnapshotJson != null ? choicesSnapshotJson : "[]";
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public static QuestionVersion create(UUID questionId, int versionNo, String content, String explanation, String choicesSnapshotJson) {
        return new QuestionVersion(UUID.randomUUID(), questionId, versionNo, content, explanation, choicesSnapshotJson, Instant.now());
    }

    public UUID getId() { return id; }
    public UUID getQuestionId() { return questionId; }
    public int getVersionNo() { return versionNo; }
    public String getContent() { return content; }
    public String getExplanation() { return explanation; }
    public String getChoicesSnapshotJson() { return choicesSnapshotJson; }
    public Instant getCreatedAt() { return createdAt; }
}
