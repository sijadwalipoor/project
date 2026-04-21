package com.euro.db2performance.controller;

import com.euro.db2performance.api.ApiResponse;
import com.euro.db2performance.api.Meta;
import com.euro.db2performance.api.PageResult;
import com.euro.db2performance.domain.Kpi;
import com.euro.db2performance.domain.Package;
import com.euro.db2performance.domain.TrendPoint;
import com.euro.db2performance.enums.MetricType;
import com.euro.db2performance.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/kpis")
    public ApiResponse<Kpi> getKpis(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ApiResponse.ok(dashboardService.getKpis(from, to));
    }

    @GetMapping("/metrics-trend")
    public ApiResponse<List<TrendPoint>> getMetricsTrend(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ApiResponse.ok(dashboardService.getTrend(from, to));
    }

    @GetMapping("/worst-packages")
    public ApiResponse<List<Package>> getWorstPackages(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "15") int pageSize,
            @RequestParam(defaultValue = "DB2_CPU") String sortBy) {

        MetricType metric = MetricType.valueOf(sortBy.toUpperCase());
        PageResult<Package> result = dashboardService.getWorstPackages(from, to, pageNumber, pageSize, metric);

        int totalPages = pageSize > 0 ? (int) Math.ceil((double) result.getTotalItems() / pageSize) : 0;
        Meta meta = Meta.builder()
                .page(pageNumber)
                .pageSize(pageSize)
                .totalItems(result.getTotalItems())
                .totalPages(totalPages)
                .build();

        return ApiResponse.ok(result.getItems(), meta);
    }
}
