package com.beeacademy.backend.domain.assessment;

import com.beeacademy.backend.domain.assessment.event.QuestionCreatedEvent;
import com.beeacademy.backend.domain.assessment.event.QuestionUpdatedEvent;
import com.beeacademy.backend.domain.common.AbstractAggregateRoot;
import com.beeacademy.backend.domain.common.DomainException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Aggregate Root đại diện cho Câu hỏi trong Ngân hàng câu hỏi (UC33).
 * Đóng gói quy tắc kiểm tra tính hợp lệ của phương án trắc nghiệm,
 * tự động lưu trữ phiên bản lịch sử (QuestionVersion) và nhật ký kiểm toán (QuestionAuditLog).
 */
public class Question extends AbstractAggregateRoot {

    private final UUID id;
    private final UUID teacherId;
    private UUID categoryId;
    private UUID chapterId;
    private int grade;
    private String content;
    private String explanation;
    private QuestionDifficulty difficulty;
    private QuestionType type;
    private QuestionStatus status;
    private List<String> tags;
    private int versionNo;
    private int usageCount;
    private final Instant createdAt;
    private Instant updatedAt;

    private final List<QuestionChoice> choices;
    private final List<QuestionVersion> versions;
    private final List<QuestionAuditLog> auditLogs;

    public Question(UUID id,
                    UUID teacherId,
                    UUID categoryId,
                    UUID chapterId,
                    int grade,
                    String content,
                    String explanation,
                    QuestionDifficulty difficulty,
                    QuestionType type,
                    QuestionStatus status,
                    List<String> tags,
                    int versionNo,
                    int usageCount,
                    Instant createdAt,
                    Instant updatedAt,
                    List<QuestionChoice> choices,
                    List<QuestionVersion> versions,
                    List<QuestionAuditLog> auditLogs) {
        if (id == null) throw new DomainException("INVALID_ID", "Question ID không được null");
        if (teacherId == null) throw new DomainException("INVALID_TEACHER_ID", "Teacher ID không được null");
        if (content == null || content.isBlank()) throw new DomainException("INVALID_CONTENT", "Nội dung câu hỏi không được để trống");
        if (grade < 1 || grade > 12) throw new DomainException("INVALID_GRADE", "Khối lớp phải từ 1 đến 12");

        this.id = id;
        this.teacherId = teacherId;
        this.categoryId = categoryId;
        this.chapterId = chapterId;
        this.grade = grade;
        this.content = content.trim();
        this.explanation = explanation != null ? explanation.trim() : "";
        this.difficulty = difficulty != null ? difficulty : QuestionDifficulty.MEDIUM;
        this.type = type != null ? type : QuestionType.MULTIPLE_CHOICE;
        this.status = status != null ? status : QuestionStatus.ACTIVE;
        this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
        this.versionNo = Math.max(1, versionNo);
        this.usageCount = Math.max(0, usageCount);
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
        this.choices = choices != null ? new ArrayList<>(choices) : new ArrayList<>();
        this.versions = versions != null ? new ArrayList<>(versions) : new ArrayList<>();
        this.auditLogs = auditLogs != null ? new ArrayList<>(auditLogs) : new ArrayList<>();
    }

    /**
     * Khởi tạo câu hỏi mới trong ngân hàng.
     */
    public static Question create(UUID teacherId,
                                  UUID categoryId,
                                  UUID chapterId,
                                  int grade,
                                  String content,
                                  String explanation,
                                  QuestionDifficulty difficulty,
                                  QuestionType type,
                                  List<String> tags,
                                  List<QuestionChoice> choices) {
        validateChoices(type, choices);

        UUID questionId = UUID.randomUUID();
        Instant now = Instant.now();
        List<QuestionChoice> initialChoices = new ArrayList<>();
        if (choices != null) {
            for (QuestionChoice c : choices) {
                initialChoices.add(QuestionChoice.of(questionId, c.getContent(), c.getImageUrl(), c.isCorrect(), c.getPosition()));
            }
        }

        Question question = new Question(
                questionId,
                teacherId,
                categoryId,
                chapterId,
                grade,
                content,
                explanation,
                difficulty,
                type,
                QuestionStatus.ACTIVE,
                tags,
                1,
                0,
                now,
                now,
                initialChoices,
                new ArrayList<>(),
                new ArrayList<>()
        );

        question.auditLogs.add(QuestionAuditLog.log(questionId, teacherId, "CREATED", "Khởi tạo câu hỏi phiên bản 1"));
        question.registerDomainEvent(QuestionCreatedEvent.of(questionId, teacherId));
        return question;
    }

    /**
     * Cập nhật nội dung câu hỏi: Tự động lưu vết QuestionVersion và ghi QuestionAuditLog (UC33).
     */
    public void update(UUID actorTeacherId,
                       String newContent,
                       String newExplanation,
                       QuestionDifficulty newDifficulty,
                       List<String> newTags,
                       List<QuestionChoice> newChoices,
                       String reason) {
        if (!this.teacherId.equals(actorTeacherId)) {
            throw new DomainException("UNAUTHORIZED_EDIT", "Chỉ giáo viên sở hữu mới có quyền chỉnh sửa câu hỏi này");
        }
        if (newContent == null || newContent.isBlank()) {
            throw new DomainException("INVALID_CONTENT", "Nội dung câu hỏi không được để trống");
        }
        validateChoices(this.type, newChoices);

        // 1. Lưu snapshot phiên bản hiện tại trước khi sửa
        this.versions.add(QuestionVersion.create(
                this.id,
                this.versionNo,
                this.content,
                this.explanation,
                serializeChoices(this.choices)
        ));

        // 2. Tăng số hiệu phiên bản
        this.versionNo++;
        this.content = newContent.trim();
        this.explanation = newExplanation != null ? newExplanation.trim() : "";
        this.difficulty = newDifficulty != null ? newDifficulty : this.difficulty;
        this.tags = newTags != null ? new ArrayList<>(newTags) : this.tags;

        // 3. Thay thế các phương án lựa chọn
        this.choices.clear();
        if (newChoices != null) {
            for (QuestionChoice c : newChoices) {
                this.choices.add(QuestionChoice.of(this.id, c.getContent(), c.getImageUrl(), c.isCorrect(), c.getPosition()));
            }
        }

        Instant now = Instant.now();
        this.updatedAt = now;

        // 4. Ghi nhận audit log và bắn domain event
        String auditDetail = (reason != null && !reason.isBlank()) ? reason.trim() : "Cập nhật câu hỏi lên phiên bản " + this.versionNo;
        this.auditLogs.add(QuestionAuditLog.log(this.id, actorTeacherId, "UPDATED", auditDetail));
        registerDomainEvent(QuestionUpdatedEvent.of(this.id, this.teacherId, this.versionNo));
    }

    public void deactivate(UUID actorTeacherId, String reason) {
        if (!this.teacherId.equals(actorTeacherId)) {
            throw new DomainException("UNAUTHORIZED_ACTION", "Không có quyền vô hiệu hóa câu hỏi");
        }
        this.status = QuestionStatus.INACTIVE;
        this.updatedAt = Instant.now();
        this.auditLogs.add(QuestionAuditLog.log(this.id, actorTeacherId, "DEACTIVATED", reason != null ? reason : "Vô hiệu hóa câu hỏi"));
    }

    public void activate(UUID actorTeacherId) {
        if (!this.teacherId.equals(actorTeacherId)) {
            throw new DomainException("UNAUTHORIZED_ACTION", "Không có quyền kích hoạt câu hỏi");
        }
        this.status = QuestionStatus.ACTIVE;
        this.updatedAt = Instant.now();
        this.auditLogs.add(QuestionAuditLog.log(this.id, actorTeacherId, "ACTIVATED", "Kích hoạt lại câu hỏi"));
    }

    public void incrementUsage() {
        this.usageCount++;
    }

    private static void validateChoices(QuestionType type, List<QuestionChoice> choices) {
        if (type == QuestionType.MULTIPLE_CHOICE || type == QuestionType.TRUE_FALSE) {
            if (choices == null || choices.size() < 2) {
                throw new DomainException("INSUFFICIENT_CHOICES", "Câu hỏi trắc nghiệm phải có ít nhất 2 phương án lựa chọn");
            }
            long correctCount = choices.stream().filter(QuestionChoice::isCorrect).count();
            if (correctCount < 1) {
                throw new DomainException("NO_CORRECT_CHOICE", "Câu hỏi trắc nghiệm phải có ít nhất 1 phương án đúng");
            }
        }
    }

    private static String serializeChoices(List<QuestionChoice> choices) {
        if (choices == null || choices.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < choices.size(); i++) {
            QuestionChoice c = choices.get(i);
            sb.append("{\"position\":").append(c.getPosition())
                    .append(",\"content\":\"").append(c.getContent().replace("\"", "\\\""))
                    .append("\",\"isCorrect\":").append(c.isCorrect()).append("}");
            if (i < choices.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    public UUID getId() { return id; }
    public UUID getTeacherId() { return teacherId; }
    public UUID getCategoryId() { return categoryId; }
    public UUID getChapterId() { return chapterId; }
    public int getGrade() { return grade; }
    public String getContent() { return content; }
    public String getExplanation() { return explanation; }
    public QuestionDifficulty getDifficulty() { return difficulty; }
    public QuestionType getType() { return type; }
    public QuestionStatus getStatus() { return status; }
    public List<String> getTags() { return Collections.unmodifiableList(tags); }
    public int getVersionNo() { return versionNo; }
    public int getUsageCount() { return usageCount; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<QuestionChoice> getChoices() { return Collections.unmodifiableList(choices); }
    public List<QuestionVersion> getVersions() { return Collections.unmodifiableList(versions); }
    public List<QuestionAuditLog> getAuditLogs() { return Collections.unmodifiableList(auditLogs); }
}
