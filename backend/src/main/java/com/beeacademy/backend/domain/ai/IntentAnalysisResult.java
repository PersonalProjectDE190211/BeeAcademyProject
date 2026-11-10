package com.beeacademy.backend.domain.ai;

/**
 * Kết quả phân tích ý định và trích xuất thực thể từ câu nói của người dùng (NLU output).
 */
public record IntentAnalysisResult(
        ChatbotIntent intent,
        SubjectCode subject,
        Integer grade,
        String learningGoal,
        Integer maxBudget,
        String sortBy,
        boolean isCorrection
) {

    public static IntentAnalysisResult of(
            ChatbotIntent intent,
            SubjectCode subject,
            Integer grade,
            String learningGoal,
            Integer maxBudget,
            String sortBy,
            boolean isCorrection
    ) {
        return new IntentAnalysisResult(
                intent != null ? intent : ChatbotIntent.GENERAL_QUESTION,
                subject,
                grade,
                learningGoal,
                maxBudget,
                sortBy,
                isCorrection
        );
    }

    public CourseSearchFilter toFilter() {
        return new CourseSearchFilter(subject, grade, learningGoal, maxBudget, sortBy);
    }
}

