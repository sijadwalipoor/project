package com.euro.db2performance.repository;

import com.euro.db2performance.domain.Kpi;
import com.euro.db2performance.domain.Package;
import com.euro.db2performance.domain.TrendPoint;
import com.euro.db2performance.enums.MetricType;
import com.euro.db2performance.mapper.KpiRowMapper;
import com.euro.db2performance.mapper.PackageRowMapper;
import com.euro.db2performance.mapper.TrendPointRowMapper;
import com.euro.db2performance.util.Db2CatalogConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final KpiRowMapper kpiRowMapper;
    private final TrendPointRowMapper trendPointRowMapper;
    private final PackageRowMapper packageRowMapper;

    public Kpi findKpis(String collection, LocalDateTime from, LocalDateTime to) {
        String sql = """
                SELECT
                    COALESCE(SUM(DB2_CPU), 0)     AS TOTAL_CPU,
                    COALESCE(SUM(DB2_ELAPSED), 0) AS TOTAL_ELAPSED,
                    COALESCE(SUM(GETPAGES), 0)    AS TOTAL_GETPAGES,
                    COALESCE(SUM(SQL_CALLS), 0)   AS TOTAL_CALLS
                FROM %s
                WHERE COLLECTION = :collection
                  AND INTERVAL_START BETWEEN :from AND :to
                """.formatted(Db2CatalogConstants.CQM_SUMM_METRICS);

        return jdbcTemplate.queryForObject(sql, params(collection, from, to), kpiRowMapper);
    }

    public List<TrendPoint> findTrendMetrics(String collection, LocalDateTime from, LocalDateTime to) {
        String sql = """
                SELECT
                    INTERVAL_START,
                    COALESCE(SUM(DB2_CPU), 0)     AS DB2_CPU,
                    COALESCE(SUM(DB2_ELAPSED), 0) AS DB2_ELAPSED,
                    COALESCE(SUM(GETPAGES), 0)    AS GETPAGES,
                    COALESCE(SUM(SQL_CALLS), 0)   AS SQL_CALLS
                FROM %s
                WHERE COLLECTION = :collection
                  AND INTERVAL_START BETWEEN :from AND :to
                GROUP BY INTERVAL_START
                ORDER BY INTERVAL_START
                """.formatted(Db2CatalogConstants.CQM_SUMM_METRICS);

        return jdbcTemplate.query(sql, params(collection, from, to), trendPointRowMapper);
    }

    public List<TrendPoint> findTrendMetricsByPackage(String collection, String program,
                                                      LocalDateTime from, LocalDateTime to) {
        String sql = """
                SELECT
                    INTERVAL_START,
                    COALESCE(SUM(DB2_CPU), 0)     AS DB2_CPU,
                    COALESCE(SUM(DB2_ELAPSED), 0) AS DB2_ELAPSED,
                    COALESCE(SUM(GETPAGES), 0)    AS GETPAGES,
                    COALESCE(SUM(SQL_CALLS), 0)   AS SQL_CALLS
                FROM %s
                WHERE COLLECTION = :collection
                  AND PROGRAM = :program
                  AND INTERVAL_START BETWEEN :from AND :to
                GROUP BY INTERVAL_START
                ORDER BY INTERVAL_START
                """.formatted(Db2CatalogConstants.CQM_SUMM_METRICS);

        MapSqlParameterSource p = params(collection, from, to).addValue("program", program);
        return jdbcTemplate.query(sql, p, trendPointRowMapper);
    }

    public List<Package> findWorstPackages(String collection, LocalDateTime from, LocalDateTime to,
                                           int page, int pageSize, MetricType sortBy) {
        int offset = (page - 1) * pageSize;

        String sql = """
                SELECT
                    COLLECTION,
                    PROGRAM,
                    CONSISTENCY_TOKEN,
                    COALESCE(SUM(DB2_ELAPSED), 0) AS DB2_ELAPSED,
                    COALESCE(SUM(GETPAGES), 0)    AS GETPAGES,
                    COALESCE(SUM(SQL_CALLS), 0)   AS SQL_CALLS,
                    COALESCE(SUM(DB2_CPU), 0)     AS DB2_CPU
                FROM %s
                WHERE COLLECTION = :collection
                  AND INTERVAL_START BETWEEN :from AND :to
                GROUP BY COLLECTION, PROGRAM, CONSISTENCY_TOKEN
                ORDER BY %s DESC
                OFFSET :offset ROWS
                FETCH FIRST :limit ROWS ONLY
                """.formatted(Db2CatalogConstants.CQM_SUMM_METRICS, sortBy.getMetric());

        MapSqlParameterSource p = params(collection, from, to)
                .addValue("offset", offset)
                .addValue("limit", pageSize);

        return jdbcTemplate.query(sql, p, packageRowMapper);
    }

    public long countWorstPackages(String collection, LocalDateTime from, LocalDateTime to) {
        String sql = """
                SELECT COUNT(*) FROM (
                    SELECT COLLECTION, PROGRAM, CONSISTENCY_TOKEN
                    FROM %s
                    WHERE COLLECTION = :collection
                      AND INTERVAL_START BETWEEN :from AND :to
                    GROUP BY COLLECTION, PROGRAM, CONSISTENCY_TOKEN
                ) t
                """.formatted(Db2CatalogConstants.CQM_SUMM_METRICS);

        Long total = jdbcTemplate.queryForObject(sql, params(collection, from, to), Long.class);
        return total != null ? total : 0L;
    }

    private MapSqlParameterSource params(String collection, LocalDateTime from, LocalDateTime to) {
        return new MapSqlParameterSource()
                .addValue("collection", collection)
                .addValue("from", Timestamp.valueOf(from))
                .addValue("to", Timestamp.valueOf(to));
    }
}
