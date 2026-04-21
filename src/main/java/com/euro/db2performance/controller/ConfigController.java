package com.euro.db2performance.controller;

import com.euro.db2performance.api.ApiResponse;
import com.euro.db2performance.config.AppProperties;
import com.euro.db2performance.enums.MetricType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Read-only configuration endpoint consumed by the frontend to initialize the UI.
 * No PUT is exposed here — DB2 context is managed operationally via application properties.
 */
@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfigController {

    private final AppProperties appProperties;

    @GetMapping("/settings")
    public ApiResponse<Map<String, Object>> getSettings() {
        Map<String, Object> settings = new LinkedHashMap<>();
        settings.put("subsystem", appProperties.getDb2().getSubsystem());
        settings.put("collection", appProperties.getDb2().getCollection());
        settings.put("defaultIntervalHours", appProperties.getUi().getDefaultIntervalHours());
        settings.put("itemsPerPage", appProperties.getUi().getItemsPerPage());
        settings.put("statsWarningThresholdDays", appProperties.getUi().getStatsWarningThresholdDays());
        settings.put("sortMetrics", Arrays.stream(MetricType.values()).map(MetricType::getMetric).toList());
        return ApiResponse.ok(settings);
    }

    @GetMapping("/metrics")
    public ApiResponse<List<String>> getSortMetrics() {
        return ApiResponse.ok(Arrays.stream(MetricType.values()).map(MetricType::getMetric).toList());
    }
}
