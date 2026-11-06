package com.beeacademy.backend.infrastructure.assessment.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "questions")
public class QuestionJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "teacher_id", nullable = false)
    private UUID teacherId;

    @Column(name = "category_id")
    private UUID categoryId;

    @Column(name = "chapter_id")
    private UUID chapterId;

    @Column(name = "grade", nullable = false)
    private int grade;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "difficulty", nullable = false, length = 20)
    private String difficulty;

    @Column(name = "type", nullable = false, length = 20)
    private String type;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "usage_count", nullable = false)
    private int usageCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("position ASC")
    private List<QuestionChoiceJpaEntity> choices = new ArrayList<>();

    public QuestionJpaEntity() {
    }

    public QuestionJpaEntity(UUID id, UUID teacherId, UUID categoryId, UUID chapterId, int grade,
                             String content, String explanation, String difficulty, String type,
                             String status, int usageCount, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.teacherId = teacherId;
        this.categoryId = categoryId;
        this.chapterId = chapterId;
        this.grade = grade;
        this.content = content;
        this.explanation = explanation;
        this.difficulty = difficulty;
        this.type = type;
        this.status = status;
        this.usageCount = usageCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void addChoice(QuestionChoiceJpaEntity choice) {
        choices.add(choice);
        choice.setQuestion(this);
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getTeacherId() { return teacherId; }
    public void setTeacherId(UUID teacherId) { this.teacherId = teacherId; }

    public UUID getCategoryId() { return categoryId; }
    public void setCategoryId(UUID categoryId) { this.categoryId = categoryId; }

    public UUID getChapterId() { return chapterId; }
    public void setChapterId(UUID chapterId) { this.chapterId = chapterId; }

    public int getGrade() { return grade; }
    public void setGrade(int grade) { this.grade = grade; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getUsageCount() { return usageCount; }
    public void setUsageCount(int usageCount) { this.usageCount = usageCount; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public List<QuestionChoiceJpaEntity> getChoices() { return choices; }
    public void setChoices(List<QuestionChoiceJpaEntity> choices) { this.choices = choices; }
}
