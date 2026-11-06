package com.beeacademy.backend.infrastructure.assessment.persistence.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "question_choices")
public class QuestionChoiceJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private QuestionJpaEntity question;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_correct", nullable = false)
    private boolean isCorrect;

    @Column(name = "position", nullable = false)
    private int position;

    public QuestionChoiceJpaEntity() {
    }

    public QuestionChoiceJpaEntity(UUID id, QuestionJpaEntity question, String content, boolean isCorrect, int position) {
        this.id = id;
        this.question = question;
        this.content = content;
        this.isCorrect = isCorrect;
        this.position = position;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public QuestionJpaEntity getQuestion() { return question; }
    public void setQuestion(QuestionJpaEntity question) { this.question = question; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public boolean isCorrect() { return isCorrect; }
    public void setCorrect(boolean correct) { isCorrect = correct; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
}
