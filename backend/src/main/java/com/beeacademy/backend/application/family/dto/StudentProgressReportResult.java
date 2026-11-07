package com.beeacademy.backend.application.family.dto;

import java.util.List;
import java.util.UUID;

public record StudentProgressReportResult(
        UUID studentId,
        String studentEmail,
        int enrolledCoursesCount,
        int completedCoursesCount,
        List<StudentCourseProgressItem> courses
) {}

