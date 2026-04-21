package com.euro.db2performance.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Db2 db2 = new Db2();
    private Cors cors = new Cors();
    private Ui ui = new Ui();

    @Getter
    @Setter
    public static class Db2 {
        @NotBlank
        private String subsystem;
        @NotBlank
        private String collection;
    }

    @Getter
    @Setter
    public static class Cors {
        private List<String> allowedOrigins = List.of("http://localhost:3000");
    }

    @Getter
    @Setter
    public static class Ui {
        private int defaultIntervalHours = 24;
        private int itemsPerPage = 50;
        private int statsWarningThresholdDays = 30;
    }
}
