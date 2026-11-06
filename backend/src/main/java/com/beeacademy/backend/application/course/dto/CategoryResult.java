package com.beeacademy.backend.application.course.dto;

import com.beeacademy.backend.domain.course.Category;

import java.util.UUID;

public record CategoryResult(
        UUID id,
        String name,
        String slug,
        String description,
        String iconUrl,
        int displayOrder,
        boolean active
) {
    public static CategoryResult from(Category category) {
        return new CategoryResult(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.getIconUrl(),
                category.getDisplayOrder(),
                category.isActive()
        );
    }
}
