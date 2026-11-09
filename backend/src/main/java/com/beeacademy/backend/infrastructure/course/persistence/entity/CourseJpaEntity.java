package com.beeacademy.backend.infrastructure.course.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA Entity ánh xạ với bảng 'courses' (Khóa học).
 */
@Entity
@Table(name = "courses")
public class CourseJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "teacher_id", nullable = false)
    private UUID teacherId;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "slug", nullable = false, unique = true, length = 280)
    private String slug;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "thumbnail_url", length = 1000)
    private String thumbnailUrl;

    @Column(name = "price_vnd", nullable = false)
    private int priceVnd;

    @Column(name = "sale_price_vnd")
    private Integer salePriceVnd;

    @Column(name = "status", nullable = false, length = 25)
    private String status;

    @Column(name = "total_chapters", nullable = false)
    private int totalChapters;

    @Column(name = "total_lessons", nullable = false)
    private int totalLessons;

    @Column(name = "total_duration_sec", nullable = false)
    private int totalDurationSec;

    @Column(name = "avg_rating", nullable = false, precision = 3, scale = 2)
    private BigDecimal avgRating;

    @Column(name = "review_count", nullable = false)
    private int reviewCount;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("position ASC")
    @org.hibernate.annotations.BatchSize(size = 50)
    private List<ChapterJpaEntity> chapters = new ArrayList<>();

    public CourseJpaEntity() {
    }

    public CourseJpaEntity(UUID id,
                           UUID teacherId,
                           UUID categoryId,
                           String title,
                           String slug,
                           String description,
                           String thumbnailUrl,
                           int priceVnd,
                           Integer salePriceVnd,
                           String status,
                           int totalChapters,
                           int totalLessons,
                           int totalDurationSec,
                           BigDecimal avgRating,
                           int reviewCount,
                           Instant submittedAt,
                           Instant publishedAt,
                           Instant createdAt,
                           Instant updatedAt) {
        this.id = id;
        this.teacherId = teacherId;
        this.categoryId = categoryId;
        this.title = title;
        this.slug = slug;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.priceVnd = priceVnd;
        this.salePriceVnd = salePriceVnd;
        this.status = status;
        this.totalChapters = totalChapters;
        this.totalLessons = totalLessons;
        this.totalDurationSec = totalDurationSec;
        this.avgRating = avgRating != null ? avgRating : BigDecimal.ZERO;
        this.reviewCount = reviewCount;
        this.submittedAt = submittedAt;
        this.publishedAt = publishedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getTeacherId() { return teacherId; }
    public void setTeacherId(UUID teacherId) { this.teacherId = teacherId; }

    public UUID getCategoryId() { return categoryId; }
    public void setCategoryId(UUID categoryId) { this.categoryId = categoryId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public int getPriceVnd() { return priceVnd; }
    public void setPriceVnd(int priceVnd) { this.priceVnd = priceVnd; }

    public Integer getSalePriceVnd() { return salePriceVnd; }
    public void setSalePriceVnd(Integer salePriceVnd) { this.salePriceVnd = salePriceVnd; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getTotalChapters() { return totalChapters; }
    public void setTotalChapters(int totalChapters) { this.totalChapters = totalChapters; }

    public int getTotalLessons() { return totalLessons; }
    public void setTotalLessons(int totalLessons) { this.totalLessons = totalLessons; }

    public int getTotalDurationSec() { return totalDurationSec; }
    public void setTotalDurationSec(int totalDurationSec) { this.totalDurationSec = totalDurationSec; }

    public BigDecimal getAvgRating() { return avgRating; }
    public void setAvgRating(BigDecimal avgRating) { this.avgRating = avgRating; }

    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }

    public Instant getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }

    public Instant getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public List<ChapterJpaEntity> getChapters() { return chapters; }
    public void setChapters(List<ChapterJpaEntity> chapters) { this.chapters = chapters; }
}

