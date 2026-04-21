package com.euro.db2performance.repository;

import com.euro.db2performance.domain.SqlStatement;
import com.euro.db2performance.mapper.SqlStatementRowMapper;
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
public class SqlStatementRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SqlStatementRowMapper sqlStatementRowMapper;

    /**
     * Return per-statement performance metrics for a specific bind (consistency token) of a package.
     */
    public List<SqlStatement> findStatementsByBind(String collection, String packageName, String conToken) {
        String sql = """
                SELECT
                    M.COLLECTION,
                    M.PROGRAM,
                    M.CONSISTENCY_TOKEN,
                    M.STMT,
                    M.TEXT_TOKEN,
                    T.SQLTEXT,
                    COALESCE(SUM(M.DB2_CPU), 0)     AS DB2_CPU,
                    COALESCE(SUM(M.DB2_ELAPSED), 0) AS DB2_ELAPSED,
                    COALESCE(SUM(M.GETPAGES), 0)    AS GETPAGES,
                    COALESCE(SUM(M.SQL_CALLS), 0)   AS SQL_CALLS
                FROM %s M
                LEFT JOIN %s T
                  ON  M.SMFID           = T.SMFID
                  AND M.CQM_SUBSYSTEM   = T.CQM_SUBSYSTEM
                  AND M.INTERVAL_NUMBER = T.INTERVAL_NUMBER
                  AND M.INTERVAL_START  = T.INTERVAL_START
                  AND M.TEXT_TOKEN      = T.TEXT_TOKEN
                WHERE M.COLLECTION = :collection
                  AND M.PROGRAM = :program
                  AND M.CONSISTENCY_TOKEN = :conToken
                GROUP BY M.COLLECTION, M.PROGRAM, M.CONSISTENCY_TOKEN, M.STMT, M.TEXT_TOKEN, T.SQLTEXT
                ORDER BY DB2_CPU DESC
                """.formatted(Db2CatalogConstants.CQM_SUMM_METRICS, Db2CatalogConstants.CQM_SUMM_TEXT);

        return jdbcTemplate.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("collection", collection)
                        .addValue("program", packageName)
                        .addValue("conToken", conToken),
                sqlStatementRowMapper);
    }
}
