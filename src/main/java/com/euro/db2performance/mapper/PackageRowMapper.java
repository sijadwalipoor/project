package com.euro.db2performance.mapper;

import com.euro.db2performance.domain.Package;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

@Component
public class PackageRowMapper implements RowMapper<Package> {

    @Override
    public Package mapRow(ResultSet rs, int rowNum) throws SQLException {
        Package pkg = new Package();
        pkg.setCollection(rs.getString("COLLECTION"));
        pkg.setProgram(rs.getString("PROGRAM"));
        pkg.setConToken(hasColumn(rs, "CONSISTENCY_TOKEN") ? rs.getString("CONSISTENCY_TOKEN") : null);
        pkg.setDb2Cpu(rs.getLong("DB2_CPU"));
        pkg.setDb2Elapsed(rs.getLong("DB2_ELAPSED"));
        pkg.setGetPages(rs.getLong("GETPAGES"));
        pkg.setSqlCalls(rs.getLong("SQL_CALLS"));
        return pkg;
    }

    private static boolean hasColumn(ResultSet rs, String column) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        for (int i = 1; i <= md.getColumnCount(); i++) {
            if (column.equalsIgnoreCase(md.getColumnLabel(i))) return true;
        }
        return false;
    }
}
