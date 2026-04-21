package com.euro.db2performance.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Kpi {
    private Long totalCpu;
    private Long totalElapsed;
    private Long totalGetPages;
    private Long totalSqlCalls;
}
