package com.beeacademy.backend.domain.course;

import com.beeacademy.backend.domain.common.AbstractAggregateRoot;
import com.beeacademy.backend.domain.common.DomainException;

import java.util.UUID;

/**
 * Aggregate Root đại diện cho Danh mục / Môn học của khóa học (Toán, Tin học, Tiếng Anh,...).
 */
public class Category extends AbstractAggregateRoot {

    private final UUID id;
    private String name;
    private String slug;
    private String description;
    private String iconUrl;
    private int displayOrder;
    private boolean active;

    public Category(UUID id, String name, String slug, String description, String iconUrl, int displayOrder, boolean active) {
        if (id == null) throw new DomainException("INVALID_ID", "ID không được null");
        if (name == null || name.isBlank()) throw new DomainException("INVALID_NAME", "Tên danh mục không được để trống");
        if (slug == null || slug.isBlank()) throw new DomainException("INVALID_SLUG", "Slug danh mục không được để trống");

        this.id = id;
        this.name = name.trim();
        this.slug = slug.trim().toLowerCase();
        this.description = description != null ? description.trim() : "";
        this.iconUrl = iconUrl != null ? iconUrl.trim() : "";
        this.displayOrder = Math.max(0, displayOrder);
        this.active = active;
    }

    public static Category create(String name, String slug, String description, String iconUrl, int displayOrder) {
        return new Category(UUID.randomUUID(), name, slug, description, iconUrl, displayOrder, true);
    }

    public void update(String name, String slug, String description, String iconUrl, int displayOrder) {
        if (name == null || name.isBlank()) throw new DomainException("INVALID_NAME", "Tên danh mục không được để trống");
        if (slug == null || slug.isBlank()) throw new DomainException("INVALID_SLUG", "Slug danh mục không được để trống");

        this.name = name.trim();
        this.slug = slug.trim().toLowerCase();
        this.description = description != null ? description.trim() : "";
        this.iconUrl = iconUrl != null ? iconUrl.trim() : "";
        this.displayOrder = Math.max(0, displayOrder);
    }

    public void deactivate() { this.active = false; }
    public void activate() { this.active = true; }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public String getDescription() { return description; }
    public String getIconUrl() { return iconUrl; }
    public int getDisplayOrder() { return displayOrder; }
    public boolean isActive() { return active; }
}
