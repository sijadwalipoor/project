package com.euro.db2performance.repository;

import com.euro.db2performance.domain.Bind;
import com.euro.db2performance.domain.Package;
import com.euro.db2performance.domain.SqlStatement;
import com.euro.db2performance.mapper.PackageRowMapper;
import com.euro.db2performance.util.Db2CatalogConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PackageRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final PackageRowMapper packageRowMapper;
    private final BindRepository bindRepository;
    private final SqlStatementRepository sqlStatementRepository;

    /**
     * Return the top N packages in the collection ordered by CPU, enriching with binds / statements on demand.
     */
    public List<Package> findTopPackages(String collection, int limit, boolean includeBinds, boolean includeStatements) {
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
                GROUP BY COLLECTION, PROGRAM, CONSISTENCY_TOKEN
                ORDER BY DB2_CPU DESC
                FETCH FIRST :limit ROWS ONLY
                """.formatted(Db2CatalogConstants.CQM_SUMM_METRICS);

        List<Package> packages = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("collection", collection)
                        .addValue("limit", limit),
                packageRowMapper);

        if (includeBinds || includeStatements) {
            for (Package pkg : packages) {
                if (includeBinds) {
                    pkg.setBinds(bindRepository.findBindsByPackage(pkg.getCollection(), pkg.getProgram()));
                }
                if (includeStatements && pkg.getConToken() != null) {
                    pkg.setSqlStatements(
                            sqlStatementRepository.findStatementsByBind(pkg.getCollection(), pkg.getProgram(), pkg.getConToken()));
                }
            }
        }

        return packages;
    }

    /**
     * Aggregated metrics for a single package (across all its binds).
     * Returns null when the package does not exist for the collection.
     */
    public Package findPackageDetails(String collection, String packageName, boolean includeBinds, boolean includeStatements) {
        String sql = """
                SELECT
                    COLLECTION,
                    PROGRAM,
                    CAST(NULL AS VARCHAR(128)) AS CONSISTENCY_TOKEN,
                    COALESCE(SUM(DB2_ELAPSED), 0) AS DB2_ELAPSED,
                    COALESCE(SUM(GETPAGES), 0)    AS GETPAGES,
                    COALESCE(SUM(SQL_CALLS), 0)   AS SQL_CALLS,
                    COALESCE(SUM(DB2_CPU), 0)     AS DB2_CPU
                FROM %s
                WHERE COLLECTION = :collection
                  AND PROGRAM = :program
                GROUP BY COLLECTION, PROGRAM
                """.formatted(Db2CatalogConstants.CQM_SUMM_METRICS);

        List<Package> results = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("collection", collection)
                        .addValue("program", packageName),
                packageRowMapper);

        if (results.isEmpty()) return null;

        Package pkg = results.get(0);

        if (includeBinds || includeStatements) {
            List<Bind> binds = bindRepository.findBindsByPackage(pkg.getCollection(), pkg.getProgram());
            if (includeBinds) {
                pkg.setBinds(binds);
            }
            if (includeStatements && !binds.isEmpty()) {
                String currentConToken = binds.get(0).getConToken();
                pkg.setConToken(currentConToken);
                pkg.setSqlStatements(sqlStatementRepository.findStatementsByBind(
                        pkg.getCollection(), pkg.getProgram(), currentConToken));
            }
        }

        return pkg;
    }

    public List<Bind> findBindsByPackage(String collection, String packageName) {
        return bindRepository.findBindsByPackage(collection, packageName);
    }

    public List<SqlStatement> findStatementsByBind(String collection, String packageName, String conToken) {
        return sqlStatementRepository.findStatementsByBind(collection, packageName, conToken);
    }

    /**
     * Fast prefix search against the DB2 catalog, capped by `limit`.
     * Uses the SYSPACKAGE (COLLID, NAME) index so it stays cheap even with 20k+ packages.
     * Returns lightweight entries without metrics — call findPackageDetails to hydrate.
     */
    public List<Package> searchPackages(String collection, String query, int limit) {
        String sql = """
                SELECT
                    COLLID AS COLLECTION,
                    NAME   AS PROGRAM,
                    CONTOKEN AS CONSISTENCY_TOKEN,
                    CAST(0 AS BIGINT) AS DB2_ELAPSED,
                    CAST(0 AS BIGINT) AS GETPAGES,
                    CAST(0 AS BIGINT) AS SQL_CALLS,
                    CAST(0 AS BIGINT) AS DB2_CPU
                FROM %s
                WHERE COLLID = :collection
                  AND NAME LIKE :namePrefix
                ORDER BY NAME
                FETCH FIRST :limit ROWS ONLY
                """.formatted(Db2CatalogConstants.SYSPACKAGE);

        return jdbcTemplate.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("collection", collection)
                        .addValue("namePrefix", query.toUpperCase() + "%")
                        .addValue("limit", limit),
                packageRowMapper);
    }
}
