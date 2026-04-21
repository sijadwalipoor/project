package com.euro.db2performance.util;

/**
 * Centralised DB2 catalog and performance-monitoring table names.
 * Change here when the deployment target uses different schemas/views.
 */
public final class Db2CatalogConstants {
    private Db2CatalogConstants() {}

    // IBM DB2 catalog
    public static final String SYSPACKAGE = "SYSIBM.SYSPACKAGE";

    // IBM Query Monitor (CQM) summary tables
    public static final String CQM_SUMM_METRICS = "SYSTOOLS.CQM_SUMM_METRICS";
    public static final String CQM_SUMM_TEXT = "SYSTOOLS.CQM_SUMM_TEXT";
}
