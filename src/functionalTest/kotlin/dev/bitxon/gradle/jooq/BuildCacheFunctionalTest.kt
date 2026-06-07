package dev.bitxon.gradle.jooq

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class BuildCacheFunctionalTest : AbstractFunctionalTest() {

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = ["plain/h2-flyway", "plain/h2-liquibase"])
    fun `task is UP_TO_DATE on second run when inputs unchanged`(scenario: String) {
        copyScenario(scenario)

        val first = runTask("generateJooq")
        assertThat(first.task(":generateJooq")?.outcome).isEqualTo(TaskOutcome.SUCCESS)

        val second = runTask("generateJooq")
        assertThat(second.task(":generateJooq")?.outcome).isEqualTo(TaskOutcome.UP_TO_DATE)
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = ["plain/h2-flyway", "plain/h2-liquibase"])
    fun `task is FROM_CACHE when outputs deleted but inputs unchanged`(scenario: String) {
        copyScenario(scenario)
        projectDir.resolve("settings.gradle.kts").toFile()
            .appendText("\nbuildCache { local { directory = rootDir.resolve(\".gradle/build-cache\") } }\n")

        val first = runTaskWithBuildCache("generateJooq")
        assertThat(first.task(":generateJooq")?.outcome).isEqualTo(TaskOutcome.SUCCESS)

        projectDir.resolve("build/generated-sources/jooq").toFile().deleteRecursively()

        val second = runTaskWithBuildCache("generateJooq")
        assertThat(second.task(":generateJooq")?.outcome).isEqualTo(TaskOutcome.FROM_CACHE)
        assertTablesGenerated(listOf("Users", "Addresses"))
    }

    private fun runTaskWithBuildCache(task: String) = GradleRunner.create()
        .withProjectDir(projectDir.toFile())
        .withPluginClasspath()
        .withArguments(task, "--stacktrace", "--build-cache", "--no-configuration-cache")
        .forwardOutput()
        .build()
}
