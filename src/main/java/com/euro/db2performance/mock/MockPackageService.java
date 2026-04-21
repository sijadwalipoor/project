package com.euro.db2performance.mock;

import com.euro.db2performance.domain.Bind;
import com.euro.db2performance.domain.Package;
import com.euro.db2performance.domain.SqlStatement;
import com.euro.db2performance.domain.TrendPoint;
import com.euro.db2performance.exception.ResourceNotFoundException;
import com.euro.db2performance.service.PackageService;
import com.euro.db2performance.util.DateRangeValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@Profile("mock")
@RequiredArgsConstructor
public class MockPackageService implements PackageService {

    private static final int MAX_TOP_N = 500;
    private static final int MAX_SEARCH_LIMIT = 100;
    private static final int DEFAULT_SEARCH_LIMIT = 50;
    private static final int TREND_POINTS = 24;

    private final MockDataSeeder seeder;
    private final DateRangeValidator dateRangeValidator;

    @Override
    public List<Package> getPackages(int topN, boolean showBinds, boolean showSqlStatements) {
        if (topN <= 0 || topN > MAX_TOP_N) {
            throw new IllegalArgumentException("topN must be between 1 and " + MAX_TOP_N);
        }
        return seeder.allPackages().stream()
                .sorted(Comparator.comparingLong(Package::getDb2Cpu).reversed())
                .limit(topN)
                .map(pkg -> enrich(pkg, showBinds, showSqlStatements))
                .toList();
    }

    @Override
    public List<Package> searchPackages(String query, int limit) {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Search query must be at least 1 character");
        }
        int capped = (limit <= 0) ? DEFAULT_SEARCH_LIMIT : Math.min(limit, MAX_SEARCH_LIMIT);
        String prefix = query.trim().toUpperCase();
        return seeder.allPackages().stream()
                .filter(pkg -> pkg.getProgram().startsWith(prefix))
                .sorted(Comparator.comparing(Package::getProgram))
                .limit(capped)
                .toList();
    }

    @Override
    public Package getPackageDetails(String packageName, boolean showBinds, boolean showSqlStatements) {
        Package pkg = seeder.getCatalog().get(packageName);
        if (pkg == null) {
            throw new ResourceNotFoundException("Package not found: " + packageName);
        }
        return enrich(pkg, showBinds, showSqlStatements);
    }

    @Override
    public List<Bind> getBindsByPackage(String packageName) {
        List<Bind> binds = seeder.getBindsByProgram().get(packageName);
        if (binds == null || binds.isEmpty()) {
            throw new ResourceNotFoundException("No binds found for package: " + packageName);
        }
        return binds;
    }

    @Override
    public List<SqlStatement> getStatementsByBind(String packageName, String conToken) {
        return seeder.getStatementsByConToken().getOrDefault(conToken, List.of());
    }

    @Override
    public List<TrendPoint> getMetricsTrend(String packageName, LocalDateTime from, LocalDateTime to) {
        dateRangeValidator.validate(from, to);
        Package pkg = seeder.getCatalog().get(packageName);
        if (pkg == null) return List.of();

        long totalMinutes = Math.max(1, Duration.between(from, to).toMinutes());
        long stepMinutes = Math.max(1, totalMinutes / TREND_POINTS);
        double perPointCpu = pkg.getDb2Cpu() / (double) TREND_POINTS;
        double perPointElapsed = pkg.getDb2Elapsed() / (double) TREND_POINTS;
        double perPointGetPages = pkg.getGetPages() / (double) TREND_POINTS;
        double perPointSqlCalls = pkg.getSqlCalls() / (double) TREND_POINTS;

        Random rng = new Random(from.toLocalDate().toEpochDay() ^ packageName.hashCode());
        List<TrendPoint> points = new ArrayList<>(TREND_POINTS);
        for (int i = 0; i < TREND_POINTS; i++) {
            double jitter = 0.7 + rng.nextDouble() * 0.6;
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

    private Package enrich(Package base, boolean showBinds, boolean showSqlStatements) {
        Package.PackageBuilder b = Package.builder()
                .collection(base.getCollection())
                .program(base.getProgram())
                .conToken(base.getConToken())
                .db2Cpu(base.getDb2Cpu())
                .db2Elapsed(base.getDb2Elapsed())
                .getPages(base.getGetPages())
                .sqlCalls(base.getSqlCalls())
                .binds(new ArrayList<>())
                .sqlStatements(new ArrayList<>());

        if (showBinds) {
            List<Bind> binds = seeder.getBindsByProgram().getOrDefault(base.getProgram(), List.of());
            b.binds(new ArrayList<>(binds));
        }
        if (showSqlStatements) {
            Map<String, List<SqlStatement>> statements = seeder.getStatementsByConToken();
            List<SqlStatement> all = new ArrayList<>();
            for (Bind bind : seeder.getBindsByProgram().getOrDefault(base.getProgram(), List.of())) {
                all.addAll(statements.getOrDefault(bind.getConToken(), List.of()));
            }
            b.sqlStatements(all);
        }
        return b.build();
    }
}
