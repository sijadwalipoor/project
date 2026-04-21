package com.euro.db2performance.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SqlStatement {
    private String collection;
    private String program;
    private String conToken;
    private Integer statementNumber;
    private Integer seqNumber;
    private String sqlText;
    private String textToken;
    private Long totalCpu;
    private Long totalElapsed;
    private Long totalGetPages;
    private Long executionCount;
}
