package com.beeacademy.backend.infrastructure.common.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Adapter quản lý upload và cấp quyền truy cập file trên Supabase Storage.
 * - Public buckets: avatars, course-docs, attachments.
 * - Private bucket: course-videos (yêu cầu Signed URL có thời hạn).
 */
@Component
public class SupabaseStorageAdapter {

    private static final Logger log = LoggerFactory.getLogger(SupabaseStorageAdapter.class);

    @Value("${supabase.url:https://haksqgakssvmlbxtqxir.supabase.co}")
    private String supabaseUrl;

    @Value("${supabase.service-role-key:}")
    private String serviceRoleKey;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public String buildPublicUrl(String bucket, String path) {
        String cleanPath = path.startsWith("/") ? path.substring(1) : path;
        return String.format("%s/storage/v1/object/public/%s/%s", supabaseUrl, bucket, cleanPath);
    }

    public String buildSignedUrl(String bucket, String path, int expiresInSeconds) {
        // Nếu không có service-role-key -> fallback public preview cho local/dev
        if (serviceRoleKey == null || serviceRoleKey.isBlank()) {
            return buildPublicUrl(bucket, path);
        }
        try {
            String url = String.format("%s/storage/v1/object/sign/%s/%s", supabaseUrl, bucket, path);
            String requestBody = String.format("{\"expiresIn\": %d}", expiresInSeconds <= 0 ? 3600 : expiresInSeconds);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + serviceRoleKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 && response.body().contains("signedURL")) {
                // Tách signedURL từ response JSON đơn giản
                int start = response.body().indexOf("signedURL\":\"") + 12;
                int end = response.body().indexOf("\"", start);
                String relativeUrl = response.body().substring(start, end);
                return supabaseUrl + "/storage/v1" + relativeUrl;
            }
        } catch (Exception e) {
            log.error("Tạo Signed URL cho {}/{} thất bại: {}", bucket, path, e.getMessage());
        }
        return buildPublicUrl(bucket, path);
    }

    public boolean uploadFile(String bucket, String path, byte[] fileBytes, String contentType) {
        if (serviceRoleKey == null || serviceRoleKey.isBlank()) {
            log.info("[MOCK STORAGE] Upload {} bytes lên {}/{} (Content-Type: {})",
                    fileBytes != null ? fileBytes.length : 0, bucket, path, contentType);
            return true;
        }

        try {
            String url = String.format("%s/storage/v1/object/%s/%s", supabaseUrl, bucket, path);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + serviceRoleKey)
                    .header("Content-Type", contentType != null ? contentType : "application/octet-stream")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(fileBytes))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200 || response.statusCode() == 201;
        } catch (Exception e) {
            log.error("Upload file lên {}/{} thất bại: {}", bucket, path, e.getMessage());
            return false;
        }
    }
}

