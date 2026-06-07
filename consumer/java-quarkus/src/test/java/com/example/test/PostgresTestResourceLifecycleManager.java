package com.example.test;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.Map;
import org.testcontainers.postgresql.PostgreSQLContainer;

public class PostgresTestResourceLifecycleManager implements QuarkusTestResourceLifecycleManager {

    private PostgreSQLContainer container;

    @Override
    public Map<String, String> start() {
        container = new PostgreSQLContainer("postgres:17.2-alpine")
            .withDatabaseName("testdb")
            .withUsername("postgres")
            .withPassword("postgres");
        container.start();

        return Map.of(
            "quarkus.datasource.jdbc.url", container.getJdbcUrl(),
            "quarkus.datasource.username", container.getUsername(),
            "quarkus.datasource.password", container.getPassword()
        );
    }

    @Override
    public void stop() {
        if (container != null) {
            container.stop();
        }
    }
}
