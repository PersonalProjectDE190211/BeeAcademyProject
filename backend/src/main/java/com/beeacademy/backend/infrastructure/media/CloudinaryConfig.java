package com.beeacademy.backend.infrastructure.media;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Cấu hình kết nối Cloudinary SDK.
 */
@Configuration
public class CloudinaryConfig {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryConfig.class);

    @Value("${cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${cloudinary.api-key:}")
    private String apiKey;

    @Value("${cloudinary.api-secret:}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        if (cloudName == null || cloudName.isBlank() || apiKey == null || apiKey.isBlank() || apiSecret == null || apiSecret.isBlank()) {
            log.warn("Cloudinary credentials chưa được thiết lập đầy đủ trong application.yml / .env. Hệ thống sẽ hoạt động ở chế độ Mock / Safe Dev.");
        }

        Map<String, Object> config = new HashMap<>();
        config.put("cloud_name", cloudName != null ? cloudName.trim() : "bee-academy");
        config.put("api_key", apiKey != null ? apiKey.trim() : "mock_key");
        config.put("api_secret", apiSecret != null ? apiSecret.trim() : "mock_secret");
        config.put("secure", true);

        return new Cloudinary(config);
    }
}
