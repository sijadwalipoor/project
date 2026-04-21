package com.euro.db2performance.service;

import com.euro.db2performance.api.PageResult;
import com.euro.db2performance.domain.Kpi;
import com.euro.db2performance.domain.Package;
import com.euro.db2performance.domain.TrendPoint;
import com.euro.db2performance.enums.MetricType;

import java.time.LocalDateTime;
import java.util.List;

public interface DashboardService {
    Kpi getKpis(LocalDateTime from, LocalDateTime to);
    List<TrendPoint> getTrend(LocalDateTime from, LocalDateTime to);
    PageResult<Package> getWorstPackages(LocalDateTime from, LocalDateTime to, int pageNumber, int pageSize, MetricType sortBy);
}
