package dev.bitxon.gradle.jooq

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class DslCoverageFunctionalTest : AbstractFunctionalTest() {

    @ParameterizedTest(name = "{0}")
    @MethodSource("scenarios")
    fun `generates jooq sources`(scenario: String) {
        copyScenario("dsl-coverage/$scenario")

        val outputDir = when (scenario) {
            "postgres-flyway--override-all-props",
            "mysql-liquibase--override-all-props",
            "postgres-sql--override-all-props" -> "build/custom-generated/jooq"
            else -> "build/generated-sources/jooq"
        }

        runTask("generateJooq")
        assertTablesGenerated(listOf("Users", "Addresses"), outputDir)

        val result = runTask("build")
        assertThat(result.task(":build")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    companion object {
        @JvmStatic
        fun scenarios() = discoverScenarios("dsl-coverage")
    }
}
