package com.euro.db2performance.mapper;

import com.euro.db2performance.domain.Kpi;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class KpiRowMapper implements RowMapper<Kpi> {

    @Override
    public Kpi mapRow(ResultSet rs, int rowNum) throws SQLException {
        Kpi kpi = new Kpi();
        kpi.setTotalCpu(rs.getLong("TOTAL_CPU"));
        kpi.setTotalElapsed(rs.getLong("TOTAL_ELAPSED"));
        kpi.setTotalGetPages(rs.getLong("TOTAL_GETPAGES"));
        kpi.setTotalSqlCalls(rs.getLong("TOTAL_CALLS"));
        return kpi;
    }
}
