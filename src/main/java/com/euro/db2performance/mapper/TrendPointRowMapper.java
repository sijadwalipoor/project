package com.euro.db2performance.mapper;

import com.euro.db2performance.domain.TrendPoint;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

@Component
public class TrendPointRowMapper implements RowMapper<TrendPoint> {

    @Override
    public TrendPoint mapRow(ResultSet rs, int rowNum) throws SQLException {
        LocalDateTime ts = rs.getTimestamp("INTERVAL_START").toLocalDateTime();

        TrendPoint point = new TrendPoint();
        point.setTimestamp(ts);
        point.setDb2Cpu(rs.getLong("DB2_CPU"));
        point.setDb2Elapsed(rs.getLong("DB2_ELAPSED"));
        point.setGetPages(rs.getLong("GETPAGES"));
        point.setSqlCalls(rs.getLong("SQL_CALLS"));
        return point;
    }
}
