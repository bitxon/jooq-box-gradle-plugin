package dev.bitxon.gradle.jooq

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class Spring40FunctionalTest : AbstractFunctionalTest() {

    @ParameterizedTest(name = "{0}")
    @MethodSource("scenarios")
    fun `generates jooq sources`(scenario: String) {
        copyScenario("spring-4.0/$scenario")

        runTask("generateJooq")
        assertTablesGenerated(listOf("Users", "Addresses"))

        val result = runTask("build")
        assertThat(result.task(":build")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    companion object {
        @JvmStatic
        fun scenarios() = discoverScenarios("spring-4.0")
    }
}
