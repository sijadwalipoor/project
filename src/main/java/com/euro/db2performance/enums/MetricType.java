package com.euro.db2performance.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum MetricType {
    DB2_CPU("DB2_CPU"),
    DB2_ELAPSED("DB2_ELAPSED"),
    SQL_CALLS("SQL_CALLS"),
    GETPAGES("GETPAGES");

    private String metric;
}
