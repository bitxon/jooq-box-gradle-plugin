package dev.bitxon.gradle.jooq.internal.database

import dev.bitxon.gradle.jooq.extension.database.DatabaseContainerType
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.time.Duration

class ContainerFactoryTest {

    @ParameterizedTest(name = "{0} default image starts and becomes healthy")
    @EnumSource(DatabaseContainerType::class)
    fun `default image starts successfully`(type: DatabaseContainerType) {
        val container = ContainerFactory.create(
            type = type,
            image = null,
            databaseName = null,
            username = null,
            password = null,
        )
        container.withStartupTimeout(Duration.ofSeconds(30))

        try {
            container.start()
            assertThat(container.isRunning).isTrue()
        } catch (e: Exception) {
            fail("Container for $type failed to start. Logs:\n${container.logs}", e)
        } finally {
            container.stop()
        }
    }
}
