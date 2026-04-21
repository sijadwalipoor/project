package com.euro.db2performance;

import com.euro.db2performance.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class Db2PerformanceApplication {

    public static void main(String[] args) {
        SpringApplication.run(Db2PerformanceApplication.class, args);
    }
}
