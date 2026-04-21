package com.euro.db2performance.mock;

import com.euro.db2performance.api.PageResult;
import com.euro.db2performance.domain.Kpi;
import com.euro.db2performance.domain.Package;
import com.euro.db2performance.domain.TrendPoint;
import com.euro.db2performance.enums.MetricType;
import com.euro.db2performance.service.DashboardService;
import com.euro.db2performance.util.DateRangeValidator;
import com.euro.db2performance.util.PaginationValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

@Service
@Profile("mock")
@RequiredArgsConstructor
public class MockDashboardService implements DashboardService {

    private static final int TREND_POINTS = 24;

    private final MockDataSeeder seeder;
    private final DateRangeValidator dateRangeValidator;
    private final PaginationValidator paginationValidator;

    @Override
    public Kpi getKpis(LocalDateTime from, LocalDateTime to) {
        dateRangeValidator.validate(from, to);
        double scale = intervalScale(from, to);
        long cpu = 0, elapsed = 0, getPages = 0, sqlCalls = 0;
        for (Package pkg : seeder.allPackages()) {
            cpu += pkg.getDb2Cpu();
            elapsed += pkg.getDb2Elapsed();
            getPages += pkg.getGetPages();
            sqlCalls += pkg.getSqlCalls();
        }
        return Kpi.builder()
                .totalCpu((long) (cpu * scale))
                .totalElapsed((long) (elapsed * scale))
                .totalGetPages((long) (getPages * scale))
                .totalSqlCalls((long) (sqlCalls * scale))
                .build();
    }

    @Override
    public List<TrendPoint> getTrend(LocalDateTime from, LocalDateTime to) {
        dateRangeValidator.validate(from, to);
        long totalMinutes = Math.max(1, Duration.between(from, to).toMinutes());
        long stepMinutes = Math.max(1, totalMinutes / TREND_POINTS);
        Kpi baseline = getKpis(from, to);
        double perPointCpu = baseline.getTotalCpu() / (double) TREND_POINTS;
        double perPointElapsed = baseline.getTotalElapsed() / (double) TREND_POINTS;
        double perPointGetPages = baseline.getTotalGetPages() / (double) TREND_POINTS;
        double perPointSqlCalls = baseline.getTotalSqlCalls() / (double) TREND_POINTS;

        Random rng = new Random(from.toLocalDate().toEpochDay());
        List<TrendPoint> points = new java.util.ArrayList<>(TREND_POINTS);
        for (int i = 0; i < TREND_POINTS; i++) {
            double jitter = 0.7 + rng.nextDouble() * 0.6; // 0.7x..1.3x
            LocalDateTime ts = from.plusMinutes(stepMinutes * i);
            points.add(TrendPoint.builder()
                    .timestamp(ts)
                    .db2Cpu((long) (perPointCpu * jitter))
                    .db2Elapsed((long) (perPointElapsed * jitter))
                    .getPages((long) (perPointGetPages * jitter))
                    .sqlCalls((long) (perPointSqlCalls * jitter))
                    .build());
        }
        return points;
    }

    @Override
    public PageResult<Package> getWorstPackages(LocalDateTime from, LocalDateTime to,
                                                int pageNumber, int pageSize, MetricType sortBy) {
        dateRangeValidator.validate(from, to);
        paginationValidator.validate(pageNumber, pageSize);

        List<Package> sorted = new java.util.ArrayList<>(seeder.allPackages());
        sorted.sort(comparatorFor(sortBy));

        long total = sorted.size();
        int fromIdx = Math.min((pageNumber - 1) * pageSize, sorted.size());
        int toIdx = Math.min(fromIdx + pageSize, sorted.size());
        List<Package> page = sorted.subList(fromIdx, toIdx);
        return new PageResult<>(new java.util.ArrayList<>(page), total);
    }

    private Comparator<Package> comparatorFor(MetricType sortBy) {
        MetricType metric = sortBy != null ? sortBy : MetricType.DB2_CPU;
        Comparator<Package> byMetric = switch (metric) {
            case DB2_CPU -> Comparator.comparingLong(Package::getDb2Cpu);
            case DB2_ELAPSED -> Comparator.comparingLong(Package::getDb2Elapsed);
            case SQL_CALLS -> Comparator.comparingLong(Package::getSqlCalls);
            case GETPAGES -> Comparator.comparingLong(Package::getGetPages);
        };
        return byMetric.reversed();
    }

    private double intervalScale(LocalDateTime from, LocalDateTime to) {
        double hours = Math.max(1.0, Duration.between(from, to).toMinutes() / 60.0);
        return hours / 24.0;
    }
}
