package com.euro.db2performance.repository;

import com.euro.db2performance.domain.Bind;
import com.euro.db2performance.mapper.BindRowMapper;
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
public class BindRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final BindRowMapper bindRowMapper;

    /**
     * Bind history of a package ordered from most-recent to oldest bind.
     */
    public List<Bind> findBindsByPackage(String collection, String packageName) {
        String sql = """
                SELECT
                    CONSISTENCY_TOKEN,
                    CREATE_TIME,
                    VERSION
                FROM %s
                WHERE COLLID = :collection
                  AND NAME = :program
                ORDER BY CREATE_TIME DESC
                """.formatted(Db2CatalogConstants.SYSPACKAGE);

        return jdbcTemplate.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("collection", collection)
                        .addValue("program", packageName),
                bindRowMapper);
    }
}
