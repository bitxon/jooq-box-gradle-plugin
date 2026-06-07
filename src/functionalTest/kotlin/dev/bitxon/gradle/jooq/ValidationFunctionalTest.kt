package dev.bitxon.gradle.jooq

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test

class ValidationFunctionalTest : AbstractFunctionalTest() {

    private fun runBuildAndFail(task: String): BuildResult = GradleRunner.create()
        .withProjectDir(projectDir.toFile())
        .withPluginClasspath()
        .withArguments(task, "--stacktrace", "--no-configuration-cache")
        .forwardOutput()
        .buildAndFail()

    @Test
    fun `codegen package name missing throws task error`() {
        copyScenario("validation/codegen--package-name-missing")

        val result = runBuildAndFail("generateJooq")

        assertThat(result.output)
            .contains("jooq: codegen.generator.target.packageName is required")
    }

    @Test
    fun `database block duplicate throws config error`() {
        copyScenario("validation/database--block-duplicate")

        val result = runBuildAndFail("help")

        assertThat(result.output)
            .contains("jooq: database kind already set to 'CONTAINER'. Configure only one database block.")
    }

    @Test
    fun `database block missing throws config error`() {
        copyScenario("validation/database--block-missing")

        val result = runBuildAndFail("help")

        assertThat(result.output)
            .contains("jooq: configure one of: database.container { } or database.embedded { }")
    }

    @Test
    fun `database container type invalid throws config error`() {
        copyScenario("validation/database--container-type-invalid")

        val result = runBuildAndFail("help")

        assertThat(result.output)
            .contains("jooq: unknown database.container.type 'fakedb'. Valid values: POSTGRES, MYSQL, MARIADB")
    }

    @Test
    fun `database container type missing throws config error`() {
        copyScenario("validation/database--container-type-missing")

        val result = runBuildAndFail("help")

        assertThat(result.output)
            .contains("jooq: database.container.type is required")
    }

    @Test
    fun `database embedded type invalid throws config error`() {
        copyScenario("validation/database--embedded-type-invalid")

        val result = runBuildAndFail("help")

        assertThat(result.output)
            .contains("jooq: unknown database.embedded.type 'fakedb'. Valid values: H2, SQLITE, HSQLDB")
    }

    @Test
    fun `database embedded type missing throws config error`() {
        copyScenario("validation/database--embedded-type-missing")

        val result = runBuildAndFail("help")

        assertThat(result.output)
            .contains("jooq: database.embedded.type is required")
    }

    @Test
    fun `migration block duplicate throws config error`() {
        copyScenario("validation/migration--block-duplicate")

        val result = runBuildAndFail("help")

        assertThat(result.output)
            .contains("jooq: migration kind already set to 'FLYWAY'. Configure only one migration block.")
    }

    @Test
    fun `migration block missing throws config error`() {
        copyScenario("validation/migration--block-missing")

        val result = runBuildAndFail("help")

        assertThat(result.output)
            .contains("jooq: configure one of: migration.flyway { }, migration.liquibase { }, or migration.sql { }")
    }

    @Test
    fun `migration liquibase change log file missing throws task error`() {
        copyScenario("validation/migration--liquibase-change-log-file-missing")

        val result = runBuildAndFail("generateJooq")

        assertThat(result.output)
            .contains("jooq: liquibase.changeLogFile is required")
    }

    @Test
    fun `migration sql scripts missing throws config error`() {
        copyScenario("validation/migration--sql-scripts-missing")

        val result = runBuildAndFail("help")

        assertThat(result.output)
            .contains("jooq: migration.sql.scripts is required")
    }
}
