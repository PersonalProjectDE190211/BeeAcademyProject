package com.beeacademy.backend.domain.ai;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Trạng thái bộ lọc tìm kiếm khóa học trong ngữ cảnh hội thoại (Conversation Context).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CourseSearchFilter(
        SubjectCode subject,
        Integer grade,
        String learningGoal,
        Integer maxBudget,
        String sortBy
) {

    public static CourseSearchFilter empty() {
        return new CourseSearchFilter(null, null, null, null, null);
    }

    public boolean isEmpty() {
        return subject == null && grade == null && learningGoal == null && maxBudget == null && sortBy == null;
    }

    /**
     * Hợp nhất điều kiện tìm kiếm mới với điều kiện hiện có (State Merging).
     * Khi người dùng đính chính hoặc bổ sung: giữ nguyên thuộc tính cũ không bị thay đổi,
     * chỉ cập nhật các thuộc tính mới được cung cấp.
     */
    public CourseSearchFilter mergeWith(CourseSearchFilter update) {
        if (update == null) {
            return this;
        }

        SubjectCode newSubject = update.subject() != null ? update.subject() : this.subject;
        Integer newGrade = update.grade() != null ? update.grade() : this.grade;
        String newGoal = update.learningGoal() != null && !update.learningGoal().isBlank()
                ? update.learningGoal()
                : this.learningGoal;
        Integer newBudget = update.maxBudget() != null ? update.maxBudget() : this.maxBudget;
        String newSortBy = update.sortBy() != null && !update.sortBy().isBlank()
                ? update.sortBy()
                : this.sortBy;

        return new CourseSearchFilter(newSubject, newGrade, newGoal, newBudget, newSortBy);
    }
}

