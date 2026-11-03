package com.beeacademy.backend.domain.course;

import com.beeacademy.backend.domain.common.AbstractAggregateRoot;
import com.beeacademy.backend.domain.common.DomainException;

import java.time.Instant;
import java.util.UUID;

/**
 * Aggregate Root đại diện cho Đánh giá / Bình luận nhận xét của học viên cho khóa học.
 */
public class CourseReview extends AbstractAggregateRoot {

    private final UUID id;
    private final UUID courseId;
    private final UUID studentId;
    private int rating;
    private String comment;
    private boolean edited;
    private final Instant createdAt;
    private Instant updatedAt;

    public CourseReview(UUID id,
                        UUID courseId,
                        UUID studentId,
                        int rating,
                        String comment,
                        boolean edited,
                        Instant createdAt,
                        Instant updatedAt) {
        if (id == null) throw new DomainException("INVALID_ID", "ID không được null");
        if (courseId == null) throw new DomainException("INVALID_COURSE_ID", "Course ID không được null");
        if (studentId == null) throw new DomainException("INVALID_STUDENT_ID", "Student ID không được null");
        validateRating(rating);

        this.id = id;
        this.courseId = courseId;
        this.studentId = studentId;
        this.rating = rating;
        this.comment = comment != null ? comment.trim() : "";
        this.edited = edited;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    public static CourseReview create(UUID courseId, UUID studentId, int rating, String comment) {
        Instant now = Instant.now();
        return new CourseReview(UUID.randomUUID(), courseId, studentId, rating, comment, false, now, now);
    }

    public void edit(int newRating, String newComment) {
        validateRating(newRating);
        this.rating = newRating;
        this.comment = newComment != null ? newComment.trim() : "";
        this.edited = true;
        this.updatedAt = Instant.now();
    }

    private static void validateRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new DomainException("INVALID_RATING", "Điểm đánh giá phải từ 1 đến 5 sao");
        }
    }

    public UUID getId() { return id; }
    public UUID getCourseId() { return courseId; }
    public UUID getStudentId() { return studentId; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public boolean isEdited() { return edited; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
