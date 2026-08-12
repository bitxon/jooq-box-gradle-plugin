package dev.bitxon.gradle.jooq

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test

class ConfigCacheFunctionalTest : AbstractFunctionalTest() {

    @Test
    fun `configuration cache is reused on second build`() {
        copyScenario("plain/h2-flyway")

        // First run — configuration cache is stored
        val first = runTask("generateJooq")
        assertThat(first.task(":generateJooq")?.outcome).isEqualTo(TaskOutcome.SUCCESS)

        // Second run — configuration phase must be skipped
        val second = runTask("generateJooq")
        assertThat(second.output).contains("Reusing configuration cache.")
        assertThat(second.task(":generateJooq")?.outcome).isIn(TaskOutcome.SUCCESS, TaskOutcome.UP_TO_DATE)
        assertTablesGenerated(listOf("Users", "Addresses"))
    }
}
