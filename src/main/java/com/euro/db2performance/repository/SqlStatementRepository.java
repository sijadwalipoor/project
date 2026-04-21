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
                    COLLECTION,
                    PROGRAM,
                    CONSISTENCY_TOKEN,
                    STMTNO,
                    SEQNO,
                    SQL_TEXT,
                    TEXT_TOKEN,
                    COALESCE(SUM(DB2_CPU), 0)     AS DB2_CPU,
                    COALESCE(SUM(DB2_ELAPSED), 0) AS DB2_ELAPSED,
                    COALESCE(SUM(GETPAGES), 0)    AS GETPAGES,
                    COALESCE(SUM(SQL_CALLS), 0)   AS SQL_CALLS
                FROM %s
                WHERE COLLECTION = :collection
                  AND PROGRAM = :program
                  AND CONSISTENCY_TOKEN = :conToken
                GROUP BY COLLECTION, PROGRAM, CONSISTENCY_TOKEN, STMTNO, SEQNO, SQL_TEXT, TEXT_TOKEN
                ORDER BY DB2_CPU DESC
                """.formatted(Db2CatalogConstants.CQM_SUMM_TEXT);

        return jdbcTemplate.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("collection", collection)
                        .addValue("program", packageName)
                        .addValue("conToken", conToken),
                sqlStatementRowMapper);
    }
}
