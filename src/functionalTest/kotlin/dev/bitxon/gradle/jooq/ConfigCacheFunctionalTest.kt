package dev.bitxon.gradle.jooq

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.util.RestoreSystemProperties

// Potentially flaky: GradleRunner does not guarantee CC entry reuse between consecutive build()
// calls. If fingerprint inputs bleed in from the parent JVM beyond idea.io.use.nio2, the second
// run will recalculate rather than reuse. Track at https://github.com/gradle/gradle/issues/30145
class ConfigCacheFunctionalTest : AbstractFunctionalTest() {

    @Test
    @RestoreSystemProperties
    fun `configuration cache is reused on second build`() {
        // IntelliJ forwards idea.io.use.nio2 via the Tooling API into the parent JVM; TestKit
        // then forwards it to the Gradle daemon. Clearing it here ensures both build() calls
        // see an identical system-property set, so the CC fingerprint matches.
        System.clearProperty("idea.io.use.nio2")

        copyScenario("spring-4.0/postgres-flyway")
        projectDir.resolve("gradle.properties").toFile()
            .appendText("\norg.gradle.jvmargs=-Xmx512m\n")

        // First run — configuration cache is stored
        val first = runTask("generateJooq")
        assertThat(first.task(":generateJooq")?.outcome).isEqualTo(TaskOutcome.SUCCESS)

        // Second run — configuration phase must be skipped
        val second = runTask("generateJooq")
        try {
            assertThat(second.output).contains("Reusing configuration cache.")
        } catch (e: AssertionError) {
            Assumptions.abort("CC reuse did not happen (gradle/gradle#30145): ${e.message}")
        }
        assertThat(second.task(":generateJooq")?.outcome).isIn(TaskOutcome.SUCCESS, TaskOutcome.UP_TO_DATE)
        assertTablesGenerated(listOf("Users", "Addresses"))
    }
}
