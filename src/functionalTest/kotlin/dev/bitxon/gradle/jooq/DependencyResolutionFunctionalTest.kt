package dev.bitxon.gradle.jooq

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DependencyResolutionFunctionalTest : AbstractFunctionalTest() {

    @Test
    fun `directly declared - uses plugin default`() {
        copyScenario("dependency-resolution/directly-declared")

        assertThat(runDependenciesTask("jooqCodegen").output)
            .contains("org.jooq:jooq-codegen:$JOOQ_DEFAULT")
            .contains("org.jooq:jooq:$JOOQ_DEFAULT")
            .contains("org.jooq:jooq-meta:$JOOQ_DEFAULT")

        assertThat(runDependenciesTask("jooqMigration").output)
            .contains("org.flywaydb:flyway-core:$FLYWAY_DEFAULT")
            .contains("org.flywaydb:flyway-database-postgresql:$FLYWAY_DEFAULT")

        assertThat(runDependenciesTask("jooqDatabase").output)
            .contains("org.postgresql:postgresql:$POSTGRES_DEFAULT")
    }

    @Test
    fun `directly declared explicit override - declared versions win`() {
        copyScenario("dependency-resolution/directly-declared--explicit-override")

        assertThat(runDependenciesTask("jooqCodegen").output)
            .contains("org.jooq:jooq-codegen:3.18.31")
            .contains("org.jooq:jooq:3.18.31")
            .contains("org.jooq:jooq-meta:3.18.31")

        assertThat(runDependenciesTask("jooqMigration").output)
            .contains("org.flywaydb:flyway-core:10.22.0")
            .contains("org.flywaydb:flyway-database-postgresql:10.22.0")

        assertThat(runDependenciesTask("jooqDatabase").output)
            .contains("org.postgresql:postgresql:42.6.2")
    }

    @Test
    fun `directly declared implicit override - version-less declaration without BOM fails`() {
        copyScenario("dependency-resolution/directly-declared--implicit-override")

        // No BOM, no explicit version — Gradle cannot resolve any of the three configurations
        assertThat(runDependenciesTask("jooqCodegen").output)
            .contains("org.jooq:jooq-codegen FAILED")

        assertThat(runDependenciesTask("jooqMigration").output)
            .contains("org.flywaydb:flyway-core FAILED")

        assertThat(runDependenciesTask("jooqDatabase").output)
            .contains("org.postgresql:postgresql FAILED")
    }

    @Test
    fun `directly declared platform override - bom version governs`() {
        copyScenario("dependency-resolution/directly-declared--platform-override")

        // platform("org.jooq:jooq-bom:3.18.31") on jooqCodegen — group matches, counts as userVersion
        assertThat(runDependenciesTask("jooqCodegen").output)
            .contains("org.jooq:jooq-codegen -> 3.18.31")
            .contains("org.jooq:jooq:3.18.31")
            .contains("org.jooq:jooq-meta:3.18.31")

        // platform("io.dropwizard.flywaydb:flyway-bom:11.11.1") on jooqMigration — BOM on the config supplies versions
        assertThat(runDependenciesTask("jooqMigration").output)
            .contains("org.flywaydb:flyway-core -> 11.11.1")
            .contains("org.flywaydb:flyway-database-postgresql -> 11.11.1")

        // jooqDatabase: not asserted — no official BOM available for the PostgreSQL JDBC driver
    }

    @Test
    fun `spring managed - plugin configurations isolated from Spring BOM use plugin defaults`() {
        copyScenario("dependency-resolution/spring-managed")

        assertThat(runDependenciesTask("jooqCodegen").output)
            .contains("org.jooq:jooq-codegen:$JOOQ_DEFAULT")
            .contains("org.jooq:jooq:$JOOQ_DEFAULT")
            .contains("org.jooq:jooq-meta:$JOOQ_DEFAULT")

        assertThat(runDependenciesTask("jooqMigration").output)
            .contains("org.flywaydb:flyway-core:$FLYWAY_DEFAULT")
            .contains("org.flywaydb:flyway-database-postgresql:$FLYWAY_DEFAULT")

        assertThat(runDependenciesTask("jooqDatabase").output)
            .contains("org.postgresql:postgresql:$POSTGRES_DEFAULT")
    }

    @Test
    fun `spring managed explicit override - declared version wins over Spring BOM`() {
        copyScenario("dependency-resolution/spring-managed--explicit-override")

        assertThat(runDependenciesTask("jooqCodegen").output)
            .contains("org.jooq:jooq-codegen:3.18.31")
            .contains("org.jooq:jooq:3.18.31")
            .contains("org.jooq:jooq-meta:3.18.31")

        assertThat(runDependenciesTask("jooqMigration").output)
            .contains("org.flywaydb:flyway-core:10.22.0")
            .contains("org.flywaydb:flyway-database-postgresql:10.22.0")

        assertThat(runDependenciesTask("jooqDatabase").output)
            .contains("org.postgresql:postgresql:42.6.2")
    }

    @Test
    fun `spring managed implicit override - Spring BOM supplies version`() {
        copyScenario("dependency-resolution/spring-managed--implicit-override")

        // Spring dependency-management applies its BOM to ALL configurations, including custom plugin ones.
        // Spring Boot 4.0.6 manages jOOQ (3.19.32)
        assertThat(runDependenciesTask("jooqCodegen").output)
            .contains("org.jooq:jooq-codegen -> 3.19.32")
            .contains("org.jooq:jooq:3.19.32")
            .contains("org.jooq:jooq-meta:3.19.32")

        // Spring Boot 4.0.6 manages Flyway (11.14.1)
        assertThat(runDependenciesTask("jooqMigration").output)
            .contains("org.flywaydb:flyway-core -> 11.14.1")
            .contains("org.flywaydb:flyway-database-postgresql -> 11.14.1")

        // Spring Boot 4.0.6 manages Flyway (42.7.10)
        assertThat(runDependenciesTask("jooqDatabase").output)
            .contains("org.postgresql:postgresql -> 42.7.10")
    }

    @Test
    fun `platform managed - plugin configurations isolated from implementation platform use plugin defaults`() {
        copyScenario("dependency-resolution/platform-managed")

        assertThat(runDependenciesTask("jooqCodegen").output)
            .contains("org.jooq:jooq-codegen:$JOOQ_DEFAULT")
            .contains("org.jooq:jooq:$JOOQ_DEFAULT")
            .contains("org.jooq:jooq-meta:$JOOQ_DEFAULT")

        assertThat(runDependenciesTask("jooqMigration").output)
            .contains("org.flywaydb:flyway-core:$FLYWAY_DEFAULT")
            .contains("org.flywaydb:flyway-database-postgresql:$FLYWAY_DEFAULT")

        assertThat(runDependenciesTask("jooqDatabase").output)
            .contains("org.postgresql:postgresql:$POSTGRES_DEFAULT")
    }

    @Test
    fun `platform managed explicit override - declared version wins over implementation platform`() {
        copyScenario("dependency-resolution/platform-managed--explicit-override")

        assertThat(runDependenciesTask("jooqCodegen").output)
            .contains("org.jooq:jooq-codegen:3.18.31")
            .contains("org.jooq:jooq:3.18.31")
            .contains("org.jooq:jooq-meta:3.18.31")

        assertThat(runDependenciesTask("jooqMigration").output)
            .contains("org.flywaydb:flyway-core:10.22.0")
            .contains("org.flywaydb:flyway-database-postgresql:10.22.0")

        assertThat(runDependenciesTask("jooqDatabase").output)
            .contains("org.postgresql:postgresql:42.6.2")
    }

    @Test
    fun `project dep alongside plugin configs - defaults still injected despite suppression`() {
        copyScenario("dependency-resolution/directly-declared--with-project-deps")

        assertThat(runDependenciesTask("jooqCodegen").output)
            .contains("org.jooq:jooq-codegen:$JOOQ_DEFAULT")

        assertThat(runDependenciesTask("jooqMigration").output)
            .contains("org.flywaydb:flyway-core:$FLYWAY_DEFAULT")
            .contains("org.flywaydb:flyway-database-postgresql:$FLYWAY_DEFAULT")

        assertThat(runDependenciesTask("jooqDatabase").output)
            .contains("org.postgresql:postgresql:$POSTGRES_DEFAULT")
    }

    @Test
    fun `platform managed implicit override - version-less declaration without BOM on custom config fails`() {
        copyScenario("dependency-resolution/platform-managed--implicit-override")

        // platform() on implementation does not reach custom plugin configurations —
        // Gradle cannot resolve any of the three configurations
        assertThat(runDependenciesTask("jooqCodegen").output)
            .contains("org.jooq:jooq-codegen FAILED")

        assertThat(runDependenciesTask("jooqMigration").output)
            .contains("org.flywaydb:flyway-core FAILED")

        assertThat(runDependenciesTask("jooqDatabase").output)
            .contains("org.postgresql:postgresql FAILED")
    }

}
