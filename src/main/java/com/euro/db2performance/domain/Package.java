package com.euro.db2performance.domain;

import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Package {
    private String collection;
    private String program;
    private String conToken;
    private Long db2Elapsed;
    private Long getPages;
    private Long sqlCalls;
    private Long db2Cpu;

    @Builder.Default
    private List<Bind> binds = new ArrayList<>();

    @Builder.Default
    private List<SqlStatement> sqlStatements = new ArrayList<>();
}
