package com.beeacademy.backend.api.common;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Controller kiểm tra trạng thái hoạt động công khai (Smoke Test / Uptime Monitor).
 */
@Tag(name = "0. Hệ thống & Giám sát (Health)", description = "API kiểm tra trạng thái sẵn sàng của backend")
@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Operation(summary = "Kiểm tra trạng thái hệ thống", description = "Trả về trạng thái UP và thời gian máy chủ")
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getHealth() {
        Map<String, Object> payload = Map.of(
                "status", "UP",
                "service", "bee-academy-backend",
                "timestamp", Instant.now().toString()
        );
        return ResponseEntity.ok(ApiResponse.success("Hệ thống hoạt động bình thường", payload));
    }
}
