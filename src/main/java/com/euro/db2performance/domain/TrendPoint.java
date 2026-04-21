package com.euro.db2performance.domain;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendPoint {
    private LocalDateTime timestamp;
    private Long db2Cpu;
    private Long db2Elapsed;
    private Long getPages;
    private Long sqlCalls;
}
