package com.example.test;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfig {

    @Bean
    @ServiceConnection
    public PostgreSQLContainer postgreSQLContainer() {
        return new PostgreSQLContainer("postgres:17.2-alpine")
            .withDatabaseName("testdb")
            .withUsername("postgres")
            .withPassword("postgres");
    }
}
