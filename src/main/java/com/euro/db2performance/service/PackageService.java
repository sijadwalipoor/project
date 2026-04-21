package com.euro.db2performance.service;

import com.euro.db2performance.domain.Bind;
import com.euro.db2performance.domain.Package;
import com.euro.db2performance.domain.SqlStatement;
import com.euro.db2performance.domain.TrendPoint;
import java.time.LocalDateTime;
import java.util.List;

public interface PackageService {
    List<Package> getPackages(int topN, boolean showBinds, boolean showSqlStatements);
    List<Package> searchPackages(String query, int limit);
    Package getPackageDetails(String packageName, boolean showBinds, boolean showSqlStatements);
    List<Bind> getBindsByPackage(String packageName);
    List<SqlStatement> getStatementsByBind(String packageName, String conToken);
    List<TrendPoint> getMetricsTrend(String packageName, LocalDateTime from, LocalDateTime to);
}
