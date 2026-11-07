package com.beeacademy.backend.domain.admin;

/**
 * Query Port (read-side) tổng hợp số liệu thống kê đa phân hệ cho Dashboard quản trị.
 * Tầng Infrastructure thực thi bằng các truy vấn tổng hợp trên Spring Data JPA.
 */
public interface AdminAnalyticsQueryPort {

    PlatformAnalytics getPlatformAnalytics();
}

