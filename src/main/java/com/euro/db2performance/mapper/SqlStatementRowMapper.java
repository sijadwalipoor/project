package com.euro.db2performance.mapper;

import com.euro.db2performance.domain.SqlStatement;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class SqlStatementRowMapper implements RowMapper<SqlStatement> {

    @Override
    public SqlStatement mapRow(ResultSet rs, int rowNum) throws SQLException {
        SqlStatement stmt = new SqlStatement();
        stmt.setCollection(rs.getString("COLLECTION"));
        stmt.setProgram(rs.getString("PROGRAM"));
        stmt.setConToken(rs.getString("CONSISTENCY_TOKEN"));
        stmt.setStatementNumber(rs.getObject("STMT") == null ? null : rs.getInt("STMT"));
        stmt.setSeqNumber(null);
        stmt.setSqlText(rs.getString("SQLTEXT"));
        stmt.setTextToken(rs.getString("TEXT_TOKEN"));
        stmt.setTotalCpu(rs.getLong("DB2_CPU"));
        stmt.setTotalElapsed(rs.getLong("DB2_ELAPSED"));
        stmt.setTotalGetPages(rs.getLong("GETPAGES"));
        stmt.setExecutionCount(rs.getLong("SQL_CALLS"));
        return stmt;
    }
}
