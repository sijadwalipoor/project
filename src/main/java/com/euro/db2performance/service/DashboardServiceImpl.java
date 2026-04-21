package com.euro.db2performance.service;

import com.euro.db2performance.api.PageResult;
import com.euro.db2performance.config.AppProperties;
import com.euro.db2performance.domain.Kpi;
import com.euro.db2performance.domain.Package;
import com.euro.db2performance.domain.TrendPoint;
import com.euro.db2performance.enums.MetricType;
import com.euro.db2performance.repository.DashboardRepository;
import com.euro.db2performance.util.DateRangeValidator;
import com.euro.db2performance.util.PaginationValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Profile("!mock")
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DashboardRepository dashboardRepository;
    private final AppProperties appProperties;
    private final DateRangeValidator dateRangeValidator;
    private final PaginationValidator paginationValidator;

    @Override
    public Kpi getKpis(LocalDateTime from, LocalDateTime to) {
        dateRangeValidator.validate(from, to);
        return dashboardRepository.findKpis(collection(), from, to);
    }

    @Override
    public List<TrendPoint> getTrend(LocalDateTime from, LocalDateTime to) {
        dateRangeValidator.validate(from, to);
        return dashboardRepository.findTrendMetrics(collection(), from, to);
    }

    @Override
    public PageResult<Package> getWorstPackages(LocalDateTime from, LocalDateTime to,
                                                int pageNumber, int pageSize, MetricType sortBy) {
        dateRangeValidator.validate(from, to);
        paginationValidator.validate(pageNumber, pageSize);

        List<Package> items = dashboardRepository.findWorstPackages(collection(), from, to, pageNumber, pageSize, sortBy);
        long total = dashboardRepository.countWorstPackages(collection(), from, to);
        return new PageResult<>(items, total);
    }

    private String collection() {
        return appProperties.getDb2().getCollection();
    }
}
