package com.euro.db2performance.mock;

import com.euro.db2performance.config.AppProperties;
import com.euro.db2performance.domain.Bind;
import com.euro.db2performance.domain.Package;
import com.euro.db2performance.domain.SqlStatement;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Component
@Profile("mock")
@RequiredArgsConstructor
public class MockDataSeeder {

    private static final long SEED = 20260421L;
    private static final int PACKAGES_PER_PREFIX = 400;
    private static final String[] PREFIXES = {"ORDER", "CUST", "INVNT", "BATCH", "RPT"};
    private static final String[] VERSIONS = {"V1.0", "V1.1", "V2.0", "V2.1", "V3.0"};
    private static final DateTimeFormatter BIND_TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AppProperties appProperties;

    @Getter
    private final Map<String, Package> catalog = new LinkedHashMap<>();

    @Getter
    private final Map<String, List<Bind>> bindsByProgram = new HashMap<>();

    @Getter
    private final Map<String, List<SqlStatement>> statementsByConToken = new HashMap<>();

    @PostConstruct
    public void seed() {
        String collection = appProperties.getDb2().getCollection();
        Random rng = new Random(SEED);

        for (String prefix : PREFIXES) {
            for (int i = 1; i <= PACKAGES_PER_PREFIX; i++) {
                String program = String.format("%s%04d", prefix, i);
                long cpu = 500L + rng.nextInt(45_000);
                long elapsed = cpu + rng.nextInt(60_000);
                long getPages = 1_000L + rng.nextInt(900_000);
                long sqlCalls = 50L + rng.nextInt(15_000);

                List<Bind> binds = buildBinds(program, rng);
                String primaryCon = binds.get(0).getConToken();

                Package pkg = Package.builder()
                        .collection(collection)
                        .program(program)
                        .conToken(primaryCon)
                        .db2Cpu(cpu)
                        .db2Elapsed(elapsed)
                        .getPages(getPages)
                        .sqlCalls(sqlCalls)
                        .binds(new ArrayList<>())
                        .sqlStatements(new ArrayList<>())
                        .build();

                catalog.put(program, pkg);
                bindsByProgram.put(program, binds);

                for (Bind bind : binds) {
                    statementsByConToken.put(bind.getConToken(), buildStatements(collection, program, bind.getConToken(), rng));
                }
            }
        }
    }

    private List<Bind> buildBinds(String program, Random rng) {
        int count = 2 + rng.nextInt(2); // 2..3 binds
        List<Bind> binds = new ArrayList<>(count);
        LocalDateTime base = LocalDateTime.of(2025, 1, 1, 8, 0);
        for (int i = 0; i < count; i++) {
            String conToken = String.format("CT-%s-%02d", program, i);
            LocalDateTime bindTime = base.plusDays(rng.nextInt(300) + i * 30L);
            String version = VERSIONS[rng.nextInt(VERSIONS.length)];
            binds.add(Bind.builder()
                    .conToken(conToken)
                    .bindTime(bindTime.format(BIND_TIME_FMT))
                    .version(version)
                    .build());
        }
        return binds;
    }

    private List<SqlStatement> buildStatements(String collection, String program, String conToken, Random rng) {
        int count = 20 + rng.nextInt(11); // 20..30 statements
        List<SqlStatement> stmts = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            long cpu = 10L + rng.nextInt(5_000);
            long elapsed = cpu + rng.nextInt(8_000);
            long getPages = 100L + rng.nextInt(50_000);
            long executions = 1L + rng.nextInt(2_500);
            String sqlText = sampleSql(program, i, rng);
            stmts.add(SqlStatement.builder()
                    .collection(collection)
                    .program(program)
                    .conToken(conToken)
                    .statementNumber(i)
                    .seqNumber(1)
                    .sqlText(sqlText)
                    .textToken(String.format("TT-%s-%04d", conToken, i))
                    .totalCpu(cpu)
                    .totalElapsed(elapsed)
                    .totalGetPages(getPages)
                    .executionCount(executions)
                    .build());
        }
        return stmts;
    }

    private String sampleSql(String program, int idx, Random rng) {
        String[] tables = {"ORDERS", "CUSTOMERS", "INVENTORY", "SHIPMENTS", "INVOICES", "ACCOUNTS"};
        String table = tables[rng.nextInt(tables.length)];
        int pattern = rng.nextInt(4);
        return switch (pattern) {
            case 0 -> "SELECT * FROM " + table + " WHERE ID = ? AND STATUS = 'ACTIVE' -- stmt " + idx + " (" + program + ")";
            case 1 -> "UPDATE " + table + " SET UPDATED_AT = CURRENT TIMESTAMP WHERE ID = ? -- stmt " + idx;
            case 2 -> "INSERT INTO " + table + " (ID, NAME, CREATED_AT) VALUES (?, ?, CURRENT TIMESTAMP) -- stmt " + idx;
            default -> "SELECT COUNT(*) FROM " + table + " WHERE CREATED_AT >= CURRENT DATE - 7 DAYS -- stmt " + idx;
        };
    }

    public List<Package> allPackages() {
        return new ArrayList<>(catalog.values());
    }
}
