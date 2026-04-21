package com.euro.db2performance.mapper;

import com.euro.db2performance.domain.Bind;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class BindRowMapper implements RowMapper<Bind> {
    @Override
    public Bind mapRow(ResultSet rs, int rowNum) throws SQLException {
        Bind bind = new Bind();
        bind.setConToken(rs.getString("CONSISTENCY_TOKEN"));
        bind.setBindTime(rs.getString("BINDTIME"));
        bind.setVersion(rs.getString("VERSION"));
        return bind;
    }
}
