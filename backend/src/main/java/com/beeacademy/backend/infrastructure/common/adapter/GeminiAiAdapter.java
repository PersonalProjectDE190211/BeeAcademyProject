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
 * Adapter tương tác với Google Gemini 2.5 Flash API
 * phục vụ tính năng Scan đề thi từ PDF và trợ lý chấm điểm tự luận.
 */
@Component
public class GeminiAiAdapter {

    private static final Logger log = LoggerFactory.getLogger(GeminiAiAdapter.class);

    @Value("${app.gemini.api-key:}")
    private String apiKey;

    @Value("${app.gemini.model:gemini-2.5-flash}")
    private String modelName;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    public String generateText(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Gemini API Key chưa được cấu hình. Trả về kết quả mẫu (Mock).");
            return "{\"success\": true, \"note\": \"Mock Gemini Response - Vui lòng cấu hình GEMINI_API_KEY\"}";
        }

        try {
            String url = String.format(
                    "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
                    modelName, apiKey
            );

            // Escape ký tự đặc biệt trong prompt JSON
            String escapedPrompt = prompt.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "");

            String requestBody = String.format(
                    "{\"contents\": [{\"parts\": [{\"text\": \"%s\"}]}]}",
                    escapedPrompt
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return response.body();
            } else {
                log.error("Gemini API lỗi: HTTP {} - {}", response.statusCode(), response.body());
                return null;
            }
        } catch (Exception e) {
            log.error("Gọi Gemini API thất bại: {}", e.getMessage());
            return null;
        }
    }
}

