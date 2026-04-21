package com.euro.db2performance.controller;

import com.euro.db2performance.api.ApiResponse;
import com.euro.db2performance.domain.Bind;
import com.euro.db2performance.domain.Package;
import com.euro.db2performance.domain.SqlStatement;
import com.euro.db2performance.domain.TrendPoint;
import com.euro.db2performance.service.PackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/packages")
@RequiredArgsConstructor
public class PackageController {

    private final PackageService packageService;

    @GetMapping
    public ApiResponse<List<Package>> getPackages(
            @RequestParam(defaultValue = "150") int topN,
            @RequestParam(defaultValue = "false") boolean showBinds,
            @RequestParam(defaultValue = "false") boolean showSqlStatements) {
        return ApiResponse.ok(packageService.getPackages(topN, showBinds, showSqlStatements));
    }

    @GetMapping("/search")
    public ApiResponse<List<Package>> searchPackages(
            @RequestParam("q") String query,
            @RequestParam(defaultValue = "50") int limit) {
        return ApiResponse.ok(packageService.searchPackages(query, limit));
    }

    @GetMapping("/{packageName}")
    public ApiResponse<Package> getPackageDetails(
            @PathVariable String packageName,
            @RequestParam(defaultValue = "false") boolean showBinds,
            @RequestParam(defaultValue = "false") boolean showSqlStatements) {
        return ApiResponse.ok(packageService.getPackageDetails(packageName, showBinds, showSqlStatements));
    }

    @GetMapping("/{packageName}/binds")
    public ApiResponse<List<Bind>> getBindsByPackage(@PathVariable String packageName) {
        return ApiResponse.ok(packageService.getBindsByPackage(packageName));
    }

    @GetMapping("/{packageName}/binds/{conToken}/statements")
    public ApiResponse<List<SqlStatement>> getStatementsByBind(
            @PathVariable String packageName,
            @PathVariable String conToken) {
        return ApiResponse.ok(packageService.getStatementsByBind(packageName, conToken));
    }

    @GetMapping("/{packageName}/metrics-trend")
    public ApiResponse<List<TrendPoint>> getPackageMetricsTrend(
            @PathVariable String packageName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ApiResponse.ok(packageService.getMetricsTrend(packageName, from, to));
    }
}
