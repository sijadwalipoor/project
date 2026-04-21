package com.euro.db2performance.service;

import com.euro.db2performance.config.AppProperties;
import com.euro.db2performance.domain.Bind;
import com.euro.db2performance.domain.Package;
import com.euro.db2performance.domain.SqlStatement;
import com.euro.db2performance.domain.TrendPoint;
import com.euro.db2performance.exception.ResourceNotFoundException;
import com.euro.db2performance.repository.DashboardRepository;
import com.euro.db2performance.repository.PackageRepository;
import com.euro.db2performance.util.DateRangeValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Profile("!mock")
@RequiredArgsConstructor
public class PackageServiceImpl implements PackageService {

    private static final int MAX_TOP_N = 500;
    private static final int MAX_SEARCH_LIMIT = 100;
    private static final int DEFAULT_SEARCH_LIMIT = 50;
    private static final int MIN_SEARCH_LENGTH = 1;

    private final PackageRepository packageRepository;
    private final DashboardRepository dashboardRepository;
    private final DateRangeValidator dateRangeValidator;
    private final AppProperties appProperties;

    @Override
    public List<Package> getPackages(int topN, boolean showBinds, boolean showSqlStatements) {
        if (topN <= 0 || topN > MAX_TOP_N) {
            throw new IllegalArgumentException("topN must be between 1 and " + MAX_TOP_N);
        }
        return packageRepository.findTopPackages(collection(), topN, showBinds, showSqlStatements);
    }

    @Override
    public List<Package> searchPackages(String query, int limit) {
        if (query == null || query.trim().length() < MIN_SEARCH_LENGTH) {
            throw new IllegalArgumentException("Search query must be at least " + MIN_SEARCH_LENGTH + " character(s)");
        }
        int capped = (limit <= 0) ? DEFAULT_SEARCH_LIMIT : Math.min(limit, MAX_SEARCH_LIMIT);
        return packageRepository.searchPackages(collection(), query.trim(), capped);
    }

    @Override
    public Package getPackageDetails(String packageName, boolean showBinds, boolean showSqlStatements) {
        Package pkg = packageRepository.findPackageDetails(collection(), packageName, showBinds, showSqlStatements);
        if (pkg == null) {
            throw new ResourceNotFoundException("Package not found: " + packageName);
        }
        return pkg;
    }

    @Override
    public List<Bind> getBindsByPackage(String packageName) {
        List<Bind> binds = packageRepository.findBindsByPackage(collection(), packageName);
        if (binds.isEmpty()) {
            throw new ResourceNotFoundException("No binds found for package: " + packageName);
        }
        return binds;
    }

    @Override
    public List<SqlStatement> getStatementsByBind(String packageName, String conToken) {
        return packageRepository.findStatementsByBind(collection(), packageName, conToken);
    }

    @Override
    public List<TrendPoint> getMetricsTrend(String packageName, LocalDateTime from, LocalDateTime to) {
        dateRangeValidator.validate(from, to);
        return dashboardRepository.findTrendMetricsByPackage(collection(), packageName, from, to);
    }

    private String collection() {
        return appProperties.getDb2().getCollection();
    }
}
