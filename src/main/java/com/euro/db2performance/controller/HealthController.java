package com.euro.db2performance.controller;

import com.euro.db2performance.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class HealthController {

    private final DataSource dataSource;

    @GetMapping("/health")
    public ApiResponse<Map<String, String>> getHealthStatus() {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("status", "UP");
        body.put("database", probeDatabase());
        return ApiResponse.ok(body);
    }

    private String probeDatabase() {
        try (Connection conn = dataSource.getConnection()) {
            return conn.isValid(2) ? "UP" : "DOWN";
        } catch (Exception e) {
            log.warn("Database probe failed: {}", e.getMessage());
            return "DOWN";
        }
    }
}
